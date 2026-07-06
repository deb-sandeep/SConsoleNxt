#!/usr/bin/env python3
"""
generate_gource_log.py

Connects to the sconsolenxt MySQL database, runs the validated
problem_attempt extraction query, and writes a Gource custom-format
log file (epoch|user|action|path) suitable for `gource --log-format custom`.

--- Design decisions baked into this script, carried over from the
    interactive session that produced it ---

1. Tree shape:  /{Subject}/{Topic}/{Book Ch-<n>}/{Exercise}/{ProblemType}/
                {ProblemNumber}-{problem_id}.{problem_type}

   - syllabus_name is deliberately omitted (redundant with topic_name for
     at least one subject, "Reasoning", where the two strings coincided).
   - Book and Chapter are collapsed into one directory level, since most
     topics draw from only 1-2 books — keeping them separate would add
     tree depth without adding fan-out.
   - Exercise is kept as its own level: a chapter genuinely fans out into
     many exercises, which is where real branching (and Gource's "bloom")
     should show up.
   - chapter_name is dropped in favour of a bare "Ch-<n>" label: it
     correlates strongly with topic_name, which is already one level up.
   - The trailing "-{problem_id}" on the leaf is defence-in-depth: within
     a (book_id, chapter_num) pair, problem_key is asserted unique by the
     data owner, but that uniqueness is not enforced by a DB constraint,
     so the numeric id is appended as a free correctness backstop.

2. Action (A/M/D) logic:
   - First-ever problem_attempt row for a problem_id -> 'A'.
   - A row whose topic_id differs from the immediately preceding row's
     topic_id (for the same problem_id) -> 'A' (new branch entry after
     a topic reassignment).
   - A row with target_state in ('Purge', 'Reassign') is only classified
     'D' if the *next* row for that problem_id is absent or lands on a
     different topic_id — i.e. deletion fires only when the leaf is
     genuinely abandoned, not merely because the state label says so.
     This was a real bug found and fixed against live data: several
     'Reassign' events were followed by re-engagement on the SAME topic,
     and naively deleting on the label alone produced "modify a deleted
     file" sequences with no defined Gource behaviour.
   - Everything else -> 'M'.

3. Single actor throughout ("Daughter" in the query, renamed to the
   actual name below) -- session/track is deliberately NOT represented
   as a Gource user dimension; it's a logical time window, not an actor.

4. Leaf pseudo-extension: Gource colours nodes by the substring after
   the last '.' in the filename. The raw leaf ("<number>-<id>") has no
   dot, so problem_type is appended as a pseudo-extension after the SQL
   query returns, guaranteeing Gource's native colour-by-extension has
   something to key off.

5. Epoch conversion: MySQL DATETIME columns can come back through
   pandas as datetime64[ns], [us], or [s] depending on pandas/driver
   version. A hardcoded division factor (e.g. // 10**9 assuming ns) is
   fragile and previously produced a 1000x timeline-compression bug when
   the actual resolution was microseconds. Truncating to second
   resolution BEFORE casting to int64 sidesteps the unit ambiguity
   entirely, regardless of what resolution pandas chose.

Usage:
    pip install pandas sqlalchemy pymysql

    Reuses the same DB_HOST / DB_USER / DB_PASSWORD env vars, and the same
    port (3306) and schema (sconsolenxt), as the app's own
    src/main/resources/config/application.properties -- no separate
    credential setup needed alongside the Spring Boot app.

    export DB_HOST=localhost
    export DB_USER=your_user
    export DB_PASSWORD=your_password
    python3 generate_gource_log.py --out gource.log --actor Arunima
"""

import argparse
import os
import pandas as pd
import sys
from sqlalchemy import create_engine, text
from sqlalchemy.engine import URL

DB_PORT = 3306
DB_NAME = "sconsolenxt"

EXTRACTION_QUERY = """
                   WITH ordered AS (
                       SELECT
                           pa.*,
                           ROW_NUMBER() OVER (PARTITION BY pa.problem_id ORDER BY pa.start_time) AS attempt_seq,
                           LAG(pa.topic_id)  OVER (PARTITION BY pa.problem_id ORDER BY pa.start_time) AS prev_topic_id,
                           LEAD(pa.topic_id) OVER (PARTITION BY pa.problem_id ORDER BY pa.start_time) AS next_topic_id
                       FROM problem_attempt pa
                   )
                   SELECT
                       o.start_time                                                              AS ts,
                       :actor_name                                                                AS gource_user,
                       CONCAT('/', sub.subject_name, '/', tm.topic_name,
                              '/', COALESCE(bm.book_short_name, bm.book_name), ' Ch-', pm.chapter_num,
                              '/', SUBSTRING_INDEX(pm.problem_key, '/', 1),
                              '/', pm.problem_type,
                              '/', SUBSTRING_INDEX(pm.problem_key, '/', -1), '-', pm.id)          AS gource_path,
                       CASE
                           WHEN o.attempt_seq = 1                                            THEN 'A'
                           WHEN o.prev_topic_id IS NOT NULL AND o.prev_topic_id <> o.topic_id THEN 'A'
                           WHEN o.target_state IN ('Purge', 'Reassign')
                               AND (o.next_topic_id IS NULL OR o.next_topic_id <> o.topic_id) THEN 'D'
                           ELSE 'M'
                           END                                                                       AS gource_action,
                       o.target_state,
                       pm.problem_type                                                          AS extension
                   FROM ordered o
                            JOIN topic_master    tm  ON tm.id = o.topic_id
                            JOIN syllabus_master  syl ON syl.syllabus_name = tm.syllabus_name
                            JOIN subject_master   sub ON sub.subject_name = syl.subject_name
                            JOIN problem_master   pm  ON pm.id = o.problem_id
                            JOIN book_master      bm  ON bm.id = pm.book_id
                   ORDER BY o.start_time; \
                   """


def build_engine():
    """
    Reads DB_HOST / DB_USER / DB_PASSWORD from the environment -- the same
    three variables the Spring Boot app itself reads in
    src/main/resources/config/application.properties -- so this script
    can be run from the same shell/session as the app with no separate
    credential setup. Port (3306) and schema (sconsolenxt) are fixed to
    match that same config file, since the app doesn't parameterize them
    either.
    """
    required = ["DB_HOST", "DB_USER", "DB_PASSWORD"]
    missing = [v for v in required if not os.environ.get(v)]
    if missing:
        sys.exit(f"Missing required environment variable(s): {', '.join(missing)}")

    url = URL.create(
        "mysql+pymysql",
        username=os.environ["DB_USER"],
        password=os.environ["DB_PASSWORD"],
        host=os.environ["DB_HOST"],
        port=DB_PORT,
        database=DB_NAME,
    )
    return create_engine(url)


def fetch_events(engine, actor_name: str) -> pd.DataFrame:
    with engine.connect() as conn:
        df = pd.read_sql(text(EXTRACTION_QUERY), conn, params={"actor_name": actor_name})
    return df


def to_gource_log(df: pd.DataFrame) -> pd.DataFrame:
    """
    Applies the two post-SQL transform steps: unit-agnostic epoch
    conversion, and pseudo-extension suffix on the leaf segment.
    """
    df = df.copy()
    df["ts"] = pd.to_datetime(df["ts"])

    # Truncate to second resolution before casting, regardless of the
    # source datetime64 unit -- see module docstring, point 5.
    epoch = df["ts"].astype("datetime64[s]").astype("int64")

    # Append problem_type as a pseudo-extension on the final path segment
    # so Gource's extension-based node colouring has a dot to key off.
    path_parts = df["gource_path"].str.rsplit("/", n=1)
    prefix = path_parts.str[0]
    leaf = path_parts.str[1] + "." + df["extension"].astype(str)
    path = prefix + "/" + leaf

    out = pd.DataFrame({
        "ts": epoch,
        "user": df["gource_user"],
        "action": df["gource_action"],
        "path": path,
    })
    return out.sort_values("ts")


def write_log(out: pd.DataFrame, out_path: str):
    with open(out_path, "w") as f:
        for row in out.itertuples(index=False):
            f.write(f"{row.ts}|{row.user}|{row.action}|{row.path}\n")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--out", default="gource.log", help="Output log file path")
    parser.add_argument("--actor", default="Daughter",
                        help="Single-actor name to use as the Gource username")
    args = parser.parse_args()

    engine = build_engine()
    df = fetch_events(engine, args.actor)

    if df.empty:
        sys.exit("Query returned zero rows -- check DB connection/credentials/table state.")

    out = to_gource_log(df)
    write_log(out, args.out)

    span_days = (out["ts"].iloc[-1] - out["ts"].iloc[0]) / 86400
    print(f"Wrote {len(out)} rows to {args.out}")
    print(f"Time span: {span_days:.2f} days "
          f"({pd.Timestamp(out['ts'].iloc[0], unit='s')} -> "
          f"{pd.Timestamp(out['ts'].iloc[-1], unit='s')})")


if __name__ == "__main__":
    main()
