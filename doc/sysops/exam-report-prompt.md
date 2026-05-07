# SConsole CBT Exam Report — Generation Prompt

## Purpose

This prompt generates a structured exam performance report from two inputs: a populated exam attempt JSON file and a Gantt timeline image. No question paper is required; all data needed is contained in the JSON.

---

## Inputs

1. **exam-eval.json** — the populated exam attempt file produced by SConsole CBT after self-evaluation. Contains two layers: system telemetry (Layer 1) and joint self-evaluation (Layer 2).
2. **exam-gantt.png** — the lap timeline image for visual verification of time distribution patterns.

---

## Data model reference

Before generating the report, ingest the JSON and build a flat question-level working table. Every claim in the report must trace to a specific field in this table. Do not use the Gantt image as a data source; use it only to confirm visual patterns.

### Top-level fields

| Field | Meaning |
|---|---|
| `id` | Attempt identifier |
| `exam.type` | Exam format (MAIN = JEE Main format) |
| `exam.note` | Operator note encoding exam configuration |
| `exam.totalMarks` | Maximum marks |
| `exam.duration` | Total time in seconds |
| `score` | Marks obtained |
| `loss` | Marks lost |
| `avoidableLoss` | Avoidable subset of marks lost |
| `avoidableLossPct` | Avoidable percentage |
| `status` | COMPLETED / ABORTED |
| `attemptDate` | ISO timestamp of attempt |

### Section-level fields (`sectionAttempts[].examSection`)

| Field | Meaning |
|---|---|
| `syllabusName` | Subject, prefixed "IIT " — strip prefix for display |
| `problemType` | SCA (single correct, negative marking) or NVT (numerical value, no penalty) |
| `examSequence` | Display order |
| `numQuestions` | Questions in section |
| `correctMarks` | Marks per correct answer |
| `wrongPenalty` | Penalty per wrong answer (negative integer or 0) |

### Question-level fields (`sectionAttempts[].questionAttempts[]`)

| Field | Meaning |
|---|---|
| `examQuestion.sequence` | Question sequence number within section |
| `examQuestion.question.topicName` | Topic as stored in system |
| `examQuestion.question.sourceId` | Question bank source identifier |
| `examQuestion.question.answer` | Correct answer |
| `evaluationStatus` | CORRECT / INCORRECT / UNANSWERED |
| `answerSubmitStatus` | ANSWERED / MARKED_FOR_REVIEW / NOT_ANSWERED / NOT_VISITED |
| `answerSubmitLap` | Lap in which answer was committed (may be set even for NOT_ANSWERED — treat as last-touched lap, not committed lap) |
| `score` | Marks awarded |
| `loss` | Marks lost |
| `avoidableLoss` | Avoidable marks lost |
| `rootCause` | Self-evaluated root cause of loss (see vocabulary below) |
| `lapDurations` | Map of lap → seconds spent |
| `lapAnalysis` | Map of lap → { score, observations[], note } |
| `timeSpent` | Total time on question in seconds |

### Deriving the paper question number

The paper question number is the 1-based global sequence of questions in section order: section 1 questions first, then section 2, and so on. Sort each section's questions by `examQuestion.sequence` before numbering.

---

## Vocabulary

### Root cause categories

| Code | Avoidable | Meaning |
|---|---|---|
| STUPID | Yes | Discipline failure: insufficient read, gut-feel commit, missed obvious step |
| CALCULATION | Yes | Arithmetic or algebraic error during execution |
| LATERAL | Yes | Could not construct the approach to the solution |
| RECOLLECTION | Yes | Could not recall a formula, theorem or fact required |
| LENGTHY | No | Question is genuinely effort-heavy; completion not feasible within time budget |

PRESSURE_ABORT is a **lap-level observation tag**, not a root cause. When present, the root cause field should hold the underlying technical block (RECOLLECTION, LATERAL, CALCULATION) that the pressure caused the student to abandon.

### Observation tags

| Tag | Meaning |
|---|---|
| PERFECT_EXECUTION | Lap execution was sound; correct process followed |
| MORE_TIME_SPENT | More time was spent on this lap than warranted |
| LESS_TIME_SPENT | Less time was spent on this lap than warranted; rushed |
| BAD_DECISION | A wrong decision was made at a key point in this lap |
| INSUFFICIENT_ANALYSIS | Planning or analysis was incomplete before moving on |
| ABORT_FAILURE | Execution began but did not complete; should have aborted sooner |
| PRESSURE_ABORT | Accumulating pressure caused the question to be abandoned mid-engagement |

**PERFECT_EXECUTION on a NOT_VISITED question** means the triage decision not to engage was correct. It is not positive execution. When aggregating scores or counting, distinguish these two meanings.

### Avoidability

A question's loss is **avoidable** if `avoidableLoss > 0`. It is **unavoidable** if `loss > 0` but `avoidableLoss == 0`. The sum of avoidable and unavoidable equals total loss.

---

## Lap structure and semantics

The exam uses a six-lap strategy. Understand each lap's purpose before writing observations about it.

**L1 — First sweep and high-confidence conversion**
The student reads every question once. At the ten-second mark they make the first decision: convert in L1 or defer. If converting: spend a minimum of one to two minutes to commit an answer. If deferring: spend another five to ten seconds routing to L2 or L3. The first decision point (convert or defer) carries higher consequence than the second (L2 or L3); when uncertain at the first decision point, defer. Sub-30-second L1 commits are process anomalies (intuition without verification), even when the answer is correct. The L1 sweep must complete before any L2P activity begins.

**L2P — Planning lap**
No answers are committed in L2P. Its purpose is to plan the solution strategy for questions deferred from L1. A question is L2-ready only when the strategy can be stated in one sentence. INSUFFICIENT_ANALYSIS here is the highest-leverage failure mode: it propagates into L2 sinks, abort failures and wrong answers.

**L2 — Execution lap**
The deep-work lap for questions planned in L2P. High-confidence execution, targeted at a high probability of correct answers. An abort trigger of 90 seconds without measurable progress is a reasonable heuristic. L2 cannot recover from an incomplete L2P; the fix is always upstream.

**AMR — Optional review**
AMR is not mandatory. It exists for reviewing answers already committed in L1 or L2. Treat any AMR duration under 10 seconds as a noise touch, not actual AMR usage. Do not conflate the question count for "visited AMR" with "meaningfully used AMR."

**L3P — Landing round and triage**
L3P has two purposes: (1) decide which unresolved questions are pliable enough to attempt in L3.1 versus lost causes (no regret); (2) briefly strategise each L3.1 question before the wrestling begins. Questions receiving very short L3P reads (typically under 20 seconds) with PERFECT_EXECUTION represent correct triage decisions: the student recognised the question was not worth pursuing further. This is good judgment, not an absence of engagement. L3P time is funded by earlier laps; if L1, L2P and L2 overrun, L3P starves.

**L3.1 — Wrestling lap**
For questions routed from L3P as pliable. A soft per-question cap of 90 seconds without progress is a useful heuristic. A question stuck at 90 seconds should be re-triaged: commit a guess, abort, or continue with deliberate acknowledgment. The panic state (visible when lap notes describe time pressure explicitly) is a stop-condition; a panicked calculation is more likely wrong than right.

**L3.2 — Lost cause lap**
For questions that could not be routed to L3.1. If `lapDurations.L3.2 == 0` for all questions, state that L3.2 was not entered in this attempt and there is nothing to observe. Do not fabricate observations for a lap with no data.

---

## Computing derived values

### "Answered" in a lap
A question is counted as answered in lap X when `answerSubmitLap == X` AND `answerSubmitStatus IN (ANSWERED, MARKED_FOR_REVIEW)`. Do not count NOT_ANSWERED as answered, even if answerSubmitLap is set.

### Lap visit count
A question was visited in lap X if `lapDurations[X] > 0`.

### Meaningful AMR
AMR duration >= 30 seconds. One-second AMR touches are noise.

### MARKED_FOR_REVIEW questions
These committed an answer (the review mark is active), but at the bell the evaluation status is still UNANSWERED because no final submit occurred. They appear in the "Unanswered" column of the Answered table, and in the "4 marked for review and left at the bell" prose.

### NOT_VISITED vs NOT_ENGAGED
NOT_VISITED in submitStatus does not mean the question had zero L1 time. A question can have brief L1 sweep time (10–35 seconds) and still show NOT_VISITED, meaning it was read during the sweep but never explicitly engaged or returned to. Frame this as "never engaged" rather than "never read."

### Total time
Sum all lap durations. Cross-check: this should equal `exam.duration` (total budget) within rounding.

### Time format
Express lap durations in m:ss. Single-digit minutes are not zero-padded (so 9:04, not 09:04).

---

## Topic abbreviations

Apply these abbreviations consistently in all tables:

| Full topic name | Abbreviation |
|---|---|
| Kinematics | KIN |
| Laws of Motion | LoM |
| Work, Energy and Power | WEP |
| Atomic Structure | AS |
| States of Matter — Gases and Liquids | SOM |
| Complex Numbers | CN |
| Theory of Equations / Theory Of Equations | ToE |
| Logarithms | LOG |
| Trigonometry | TRG |

For topics not in this table, derive a 3–4 character abbreviation from the topic name and define it in a legend.

Subject abbreviations: Physics → P, Chemistry → C, Maths → M.

---

## Report structure

Generate the report in Markdown. The report has four top-level sections. Follow the structure exactly.

---

### Header

```
# Exam #{exam.id} — Performance Report

**Attempt date:** {formatted attemptDate}
**Status:** {status}
**Final score:** {score} / {exam.totalMarks}

---
```

---

### Section 1: Exam paper analysis

#### 1.1 Paper structure

Two tables. First: property/value table for Total duration (in minutes), Total marks, Total questions, Number of sections. Second: section table with columns Sec, Subject (full name), Type, Questions, Marks each, Section marks, Wrong penalty.

#### 1.2 Exam type

One sentence naming the exam format. For type = MAIN: "JEE Main format. Three subjects, two question types per subject (SCA and NVT). SCA carries negative marking; NVT does not."

#### 1.3 Topic × type distribution

Table with columns Subject (full name), Topic (full name), SCA, NVT, Total. Group by subject. Show a row per topic. No total row needed.

No section 1.4. The question paper is not required and question-level topic mapping is not included in the report.

---

### Section 2: Result analysis

#### 2.1 Total marks

Table with rows: Available, Obtained, Lost, Avoidable loss, Unavoidable loss, Avoidable loss percentage. Unavoidable loss = loss − avoidableLoss. Single numeric value column.

#### 2.2 Section-level results

Two separate sub-tables.

**2.2.1 Marks** — columns: Sec, Subj (abbreviated), Type, Available, Obtained, Lost, Avoidable. Total row.

**2.2.2 Num questions** — columns: Sec, Subj (abbreviated), Type, Total, Correct, Wrong, Unanswered. Total row.

#### 2.3 Topic-level results

Aggregate by (subject, topic) across both question types. Columns: Subj (abbreviated), Topic (abbreviated), Q, Correct, Wrong, Unans, Score, Loss, Avoidable. Total row. Group Physics first, then Chemistry, then Maths.

#### 2.4 Lap analysis

**2.4.1 Questions visited and answers committed per lap**

Table with columns: Lap, Visited, Answered, Correct, Wrong, Unanswered. One row per lap (L1 through L3.2). Use the definitions above for Visited and Answered. Correct/Wrong/Unanswered refers to the evaluation outcome of questions whose answers were committed in that lap. MFR questions committed in L3P are counted as Unanswered in the outcome columns.

Follow this table with a submit-status breakdown table (Status, Count) for ANSWERED, MARKED_FOR_REVIEW (left at bell), NOT_ANSWERED (visited, not committed), NOT_VISITED. Add prose: "Among the {n} unanswered: {a} never visited; {b} visited but not committed; {c} marked for review and left at the bell."

**2.4.2 Outcome breakdown**

Table with rows: Correct, Wrong, Unanswered or aborted. Single count column.

**2.4.3 Lap time totals and average per question**

Table with columns: Lap, Total (in minutes, 1 decimal), Time % (of exam.duration), Visited, Avg (average seconds per visited question, rounded). Total row for time only. Include only laps with Total > 0.

#### 2.5 Time analysis

**2.5.1 Time per question and per lap**

One row per question (45 rows). Columns: Q (paper question number), Subj (abbreviated), Type, Topic (abbreviated), L1, L2P, L2, AMR, L3P, L3.1, Total. All time values in m:ss format. Blank cells for laps with zero duration. Right-align all time columns. Header row uses `---:` separator for all numeric columns.

Intro line: "Durations in m:ss. Blank cells indicate the lap was not entered for that question."

**2.5.2 Total time per question — distribution highlights**

Two tables. First: ten longest engagements in descending order of Total. Second: ten shortest engagements in ascending order of Total. Both tables: columns Q, Subj (abbreviated), Topic (abbreviated), Total (m:ss), Outcome (plain language: Correct / Wrong / Not visited / Not answered / Marked, not committed).

---

### Section 3: Error analysis

#### 3.1 Lossy questions

**3.1.1 Avoidable vs unavoidable**

Table with rows Avoidable and Unavoidable. Columns: Category, Questions (count), Marks lost. Follow with a sentence naming the questions tagged as LENGTHY (unavoidable).

**3.1.2 Root cause distribution**

Summary table: Root cause, Questions, Marks lost, Avoidable. One row per root cause present in the data.

Per-question detail table: Q, Subj (abbreviated), Topic (abbreviated), Outcome, Root cause, Lost. Right-align the Lost column. Mark unavoidable losses with an asterisk prefix on the number (*4, not 4*) so that digits stay aligned. Add footnote: `\* Unavoidable loss.`

**3.1.3 Lap observations**

First table — transposed: rows are observation tags, columns are laps. Numeric cells right-aligned. Include only laps with at least one non-zero observation count.

Second table — per question: one row per lossy question, columns Q, L1, L2P, L2, AMR, L3P, L3.1. Cells contain comma-separated tag abbreviations. Use `-` for a lap that was entered but has no observations. Leave blank if the lap was not entered for that question.

Tag abbreviation legend: PE = PERFECT_EXECUTION, MT = MORE_TIME_SPENT, LT = LESS_TIME_SPENT, BD = BAD_DECISION, IA = INSUFFICIENT_ANALYSIS, AF = ABORT_FAILURE, PA = PRESSURE_ABORT.

---

### Section 4: Actionable insights

This section stands alone as a top-level section (## 4), not as a subsection of Section 3.

Introductory line: "Insights are grouped by lap in the order L1, L2P, L2, AMR, L3P, L3.1, L3.2. Each lap section has three subsections: what went right, what went wrong, and betterment."

One subsection per lap: ### L1, ### L2P, ### L2, ### AMR, ### L3P, ### L3.1, ### L3.2.

**If a lap has no data** (all lap durations zero, no analysis entries), state this in one sentence and move on. Do not invent observations. This applies in particular to L3.2, which may not have been entered.

Each lap subsection has three parts:

**What went right.** Behaviours and decisions that were correct and should be reinforced in the next attempt.

**What went wrong.** Behaviours and decisions that cost marks or time and should be avoided.

**Betterment.** Alternative approaches or procedural changes, even when the current behaviour was not detrimental.

**Formatting rules for Section 4:**

- Each observation is a separate bullet point.
- Each bullet opens with a behavioural observation and its consequence.
- Specific question references follow within the same bullet.
- Short, simple sentences. No compound joiners.
- Frame everything from the student's exam execution perspective. Not "the data shows" but "the decision cost time."
- No cross-exam comparisons. Laps are designed for question segregation, not volume targets.
- Do not assign volume or percentage targets to any lap.
- Sub-30-second L1 commits are process anomalies even when correct.
- PERFECT_EXECUTION on a not-engaged question is correct triage, not positive performance.

**Mandatory pre-publication checks for Section 4:**

Before declaring the report complete, run the following verification pass. Every specific claim involving a question number, an observation tag, a lap duration or a count must be verified against the JSON. Pattern claims ("L2P was treated as a gate") are interpretive and may rest on the data. Specific claims are checkable and must be checked.

Specific checks required:

1. Any claim that a question carried a specific observation tag (e.g. ABORT_FAILURE): verify `lapAnalysis[lap].observations` for that question.
2. Any claim about a question's duration: verify `lapDurations[lap]` for that question.
3. Any claim that a question was committed in a specific lap: verify `answerSubmitLap` and `answerSubmitStatus`.
4. Any count claim (e.g. "nine questions"): count the list and verify.
5. Any claim that L3.2 was used: verify that at least one question has `lapDurations.L3.2 > 0`. If none do, state that L3.2 was not entered.
6. Any claim about what a consequence of one event was for another (e.g. "AMR would have caught Q6"): verify the causal chain against the data. If the chain does not hold, remove the claim.
7. AMR: only count as "meaningfully used" questions with AMR >= 30s. One-second touches are noise.
8. NOT_VISITED questions: verify whether they have non-zero L1 time before writing "never read." If L1 > 0, say "never engaged" rather than "never read."

---

## Style rules

- Subject abbreviations: P, C, M.
- Topic abbreviations: as per the table above.
- Time in m:ss for per-question tables; decimal minutes for lap totals.
- Right-align all numeric columns using `---:` in Markdown table separators.
- No excessive hyphens. Alternate between commas, semicolons and colons. Reserve hyphens only for legitimate compound modifiers.
- Unavoidable losses marked with asterisk prefix (*4).
- Free-text notes from lapAnalysis may be quoted verbatim when they are the clearest expression of the mechanism. Keep quotes short.
- Avoid judgment language ("she panicked") in favour of behavioural language ("the note records that panic had set in"). The data surfaces the pattern; the student names it.
- The report is written for the student as the primary reader. The tone is factual, direct and respectful. It should be readable in one sitting by someone who lived the attempt.
