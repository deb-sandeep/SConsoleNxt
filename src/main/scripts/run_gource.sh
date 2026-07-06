#!/usr/bin/env bash
# SConsoleNxt — two-year prep arc, Gource sanity render
#
# Run this on a machine with Gource installed and a display (or Xvfb).
# It is NOT runnable inside this sandbox: Gource needs an OpenGL context,
# which this container does not have.
#
#   macOS:   brew install gource
#   Ubuntu:  sudo apt-get install gource
#
# ---------------------------------------------------------------------------
# Quick interactive preview (just watch it):
#
#   gource gource.log --log-format custom \
#       --seconds-per-day 0.3 \
#       --auto-skip-seconds 1 \
#       --file-idle-time 0 \
#       --hide mouse,progress \
#       --title "SConsoleNxt — Prep Arc"
#
# ---------------------------------------------------------------------------
# Full recipe below, with rendering to an MP4 via ffmpeg pipe.

gource ~/temp/gource.log \
  --log-format custom \
  \
  `# --- Time compression ---------------------------------------------` \
  `# 464 real days in the log. At 0.3 sim-seconds per real day, the full` \
  `# arc plays in ~140s at 60fps. Raise this for a slower, more legible` \
  `# swarm; lower it for a faster fly-through.` \
  --seconds-per-day 0.3 \
  \
  `# --- Idle handling -------------------------------------------------` \
  `# Skip forward through days with zero activity so the timeline doesn't` \
  `# stall on gaps (illness days, exam-week breaks, etc.).` \
  --auto-skip-seconds 1 \
  \
  `# --- Node decay ------------------------------------------------------` \
  `# How long (in log time units) a touched file stays at "active" size` \
  `# before shrinking toward its idle/dormant size. 0 = never shrink, files` \
  `# stay maximal forever once touched, which will read as visual clutter` \
  `# at this event density. Start here and tune by eye — this flag's exact` \
  `# unit semantics have shifted across Gource versions, so verify against` \
  `# your installed version's --help/man page rather than trust this` \
  `# value blindly.` \
  --file-idle-time 86400 \
  \
  `# --- Layout ----------------------------------------------------------` \
  --max-file-lag 0.2 \
  --dir-name-depth 3 \
  `# Depth 1 = Subject, depth 2 = Topic. Capping here means Book-Chapter` \
  `# and everything below never gets a directory label at all — that's` \
  `# the fix for "too much text" more than any fade-speed setting is:` \
  `# most of the clutter was --highlight-dirs forcing every level's name` \
  `# to stay on screen permanently, not labels lingering too long.` \
  \
  `# --- Cosmetic ----------------------------------------------------------` \
  --viewport 1600x1000 \
  --hide mouse,progress,filenames \
  `# filenames hidden entirely: at 0.3 sec/day compression, many same-day` \
  `# attempts land in the same fraction-of-a-second window, and their` \
  `# labels pile up and obscure the tree rather than reading as text.` \
  `# Try dropping "filenames" from --hide first if you want to test` \
  `# whether your installed version exposes a duration/size flag that` \
  `# keeps them but shortens their lifetime — check` \
  `# 'gource --help | grep -i file' for anything version-specific before` \
  `# assuming it doesn't exist.` \
  --dir-font-size 15 \
  --file-font-size 13 \
  --title "SConsoleNxt — Prep Arc (Mar 2025 - Jul 2026)" \
  --background-colour 000000 \
  --font-size 25 \
  --bloom-multiplier 0.2 \
  --camera-mode overview \
  --fixed-user-size \
  \
  `# --- Output ------------------------------------------------------------` \
  --output-framerate 30 \
  --output-ppm-stream - \
  | ffmpeg -y -r 30 -f image2pipe -vcodec ppm -i - \
      -vcodec libx264 -preset slow -crf 26 -pix_fmt yuv420p \
      -movflags +faststart \
      ~/temp/sconsole_prep_arc.mp4


# ---------------------------------------------------------------------------
# No video export for now — MP4s at this event density and resolution get
# large fast, and we're still tuning layout/text/timing. This runs straight
# to the interactive window (Gource's default when no --output flags are
# given). Add an --output-ppm-stream | ffmpeg pipe back once the config is
# settled and you actually want a file to keep or share.

# ---------------------------------------------------------------------------
# Notes on what the current gource.log encodes:
#
#   - Tree:   /{Subject}/{Topic}/{Book Ch-<n>}/{Exercise}/
#             {ProblemType}/{ProblemNumber}-{problem_id}.{problem_type}
#   - chapter_name is deliberately omitted in favour of a bare Ch-<n>
#     label: chapter_name correlates strongly with topic_name (already
#     one level up), so it was mostly redundant text. One residual case
#     to be aware of: if the same chapter genuinely spans two topics, it
#     will render as "Ch-<n>" under both, with nothing distinguishing why
#     — not a collision (leaves stay distinct via the topic ancestor),
#     just a minor loss of self-explanatory labelling.
#   - Book+Chapter are collapsed into one level deliberately (most topics
#     draw from just 1-2 books, so keeping them separate would add depth
#     without adding fan-out). Exercise is kept as its own level since a
#     chapter genuinely fans out into many exercises — that's where real
#     branching, and therefore bloom, should show up.
#   - The trailing "-{problem_id}" on the leaf is defence-in-depth: within
#     a (book, chapter) pair problem_key is confirmed unique by the data
#     owner, so it's not strictly required for correctness, but it's free
#     insurance since problem_key itself carries no DB-level unique
#     constraint.
#   - Leaf "extension" is a pseudo-extension appended deliberately — the
#     underlying identifier has no dot in it on its own, so Gource's
#     native file-extension colouring (which keys off the substring after
#     the last '.') would have had nothing to colour by without this. The
#     8 problem_type values (ART, CMT, LCT, MCA, MMT, NVT, SCA, SUB) will
#     each get a distinct hash-derived colour automatically — no manual
#     palette needed for this first pass.
#   - Single actor throughout ("Arunima"), per the earlier decision not
#     to represent session/track as a Gource user dimension.
#   - A/M/D actions encode the corrected Reassign/Purge logic (deletion
#     only when the next attempt on that problem_id actually lands on a
#     different topic_id, or never recurs at all).
#   - syllabus_name is deliberately omitted from the tree per the last
#     request; this also incidentally resolves the earlier /Reasoning/
#     Reasoning redundancy, since that duplication came from subject_name
#     and syllabus_name being identical strings for that one subject.
