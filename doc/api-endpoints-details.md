Below is a categorized list of all HTTP‐based (REST) endpoints defined under `com.sandy.sconsole/endpoints/rest`. For each endpoint, you’ll see:

* **Method** (GET/POST/DELETE)
* **Path** (relative to the application root)
* **Summary** (a brief description of what it does)

Endpoints are grouped into **Master APIs** (content‐management, metadata, mappings) and **Live APIs** (session, problem‐attempt, feeds).

---

## Master APIs

*(controllers under `endpoints/rest/master/`)*

### 1. **BookAPIs** (`/Master/Book`)

* **GET /Master/Book/Listing**
  Retrieves a list of all “Book” records (used to populate the master book inventory).

* **GET /Master/Book/{bookId}/ProblemSummary**
  Returns a summary of problems belonging to the specified book (e.g., counts by chapter/topic).

* **GET /Master/Book/TopicMappings**
  Fetches all existing book‐to‐topic mappings (i.e., which topics are covered by which books).

* **POST /Master/Book/{bookId}/UpdateAttribute**
  Updates one or more attributes (e.g., name, description) of the specified book. Expects a JSON payload with new values.

* **POST /Master/Book/{bookId}/{chapterNum}/SaveChapterName**
  Creates or renames a chapter within the given book. The request payload contains the new chapter name.

* **POST /Master/Book/{bookId}/{chapterNum}/{exerciseNum}/UpdateExerciseName**
  Updates the name/title of a specific exercise (identified by its sequence number) within the given chapter of the specified book.

---

### 2. **BookUploadAPIs** (`/Master/Book`)

* **POST /Master/Book/ValidateMetaFile**
  Accepts a metadata file (e.g., a CSV or JSON describing book structure) and validates its format/contents before saving. Returns validation errors (if any).

* **POST /Master/Book/SaveMeta**
  Saves or updates the metadata (e.g., chapters, exercise counts) for a book based on a previously validated metadata payload.

* **POST /Master/Book/CreateNewExercise**
  Creates a brand‐new exercise under a given book+chapter. The request body contains fields like bookId, chapterNum, question text, default difficulty, etc.

---

### 3. **ChapterTopicMappingAPIs** (`/Master/ChapterTopicMapping`)

* **GET /Master/ChapterTopicMapping**
  Retrieves all Chapter→Topic mappings currently defined (used to know which chapter is associated with which topic).

* **POST /Master/ChapterTopicMapping**
  Creates a new mapping between a chapter and a topic. Expects a JSON body with fields like `chapterId` and `topicId`.

* **DELETE /Master/ChapterTopicMapping/{mapId}**
  Deletes the mapping record identified by `mapId` (removing that chapter↔topic association).

* **POST /Master/ChapterTopicMapping/SwapAttemptSequence/{mappingId1}/{mappingId2}**
  Swaps the “attempt sequence” (i.e., the relative order) between two chapter‐topic mapping records. No request body needed—both IDs are in the URL.

* **POST /Master/ChapterTopicMapping/ToggleProblemMappingDone/{mappingId}**
  Toggles the “done” flag on the specified chapter‐topic mapping (e.g., marks it as complete/incomplete).

---

### 4. **ProblemTopicMappingAPIs** (`/Master/ProblemTopicMapping`)

* **GET /Master/ProblemTopicMapping/Book/{bookId}/Chapter/{chapterNum}**
  Fetches all problems currently mapped to the given book + chapter. Returns a list of problem IDs and their assignment status.

* **POST /Master/ProblemTopicMapping/AttachProblems/{topicChapterMapId}**
  Attaches one or more problems to the chapter‐topic mapping identified by `topicChapterMapId`. The payload contains a list of problem IDs to add.

* **POST /Master/ProblemTopicMapping/DetachProblems**
  Detaches (i.e., un‐maps) problems from their chapter‐topic association. The request body includes the mapping ID(s) or problem IDs to remove.

---

### 5. **ProblemAPIs** (`/Master/Problem`)

* **POST /Master/Problem/{problemId}/DifficultyLevel/{difficultyLevel}**
  Updates the difficulty rating for the specified problem. No GET methods are defined here—this is solely used to “set” or change a problem’s difficulty via a URL‐encoded payload.

---

### 6. **SyllabusAPIs** (`/Master/Syllabus`)

* **GET /Master/Syllabus/All**
  Returns the complete list of all syllabi (e.g., “IIT Physics,” “IIT Maths,” etc.). Each syllabus record includes its ID, name, and any descriptive metadata.

---

### 7. **TrackAPIs** (`/Master/Track`)

* **GET /Master/Track/All**
  Retrieves all “track” definitions—that is, pre‐built study tracks or curricula. Each track typically includes an ordered list of topics or chapters.

* **GET /Master/Track/CurrentTopicAssignments**
  Returns the currently assigned topics for the active track(s) (e.g., which topics are slated for study this week).

* **POST /Master/Track/{id}/SaveTopicSchedules**
  Saves or updates the schedule details (e.g., planned dates/times) for all topics under the track identified by `id`. Payload contains scheduling information (dates, start/end times).

---

### 8. **TopicAPIs** (`/Master/Topic`)

* **GET /Master/Topic/{topicId}**
  Fetches detailed information for a single topic (e.g., name, description, associated subject, parent syllabus).

* **GET /Master/Topic/All**
  Returns the full list of all topics in the system. Each topic record includes its ID, name, and associated metadata.

* **GET /Master/Topic/ProblemTypeCounts**
  Returns aggregated counts of problem types (e.g., “MCQ vs. subjective vs. numeric”) across all topics. Useful for dashboards or reports.

* **GET /Master/Topic/{topicId}/Problems**
  Retrieves all problems that are mapped to the specified topic, including their IDs, difficulty, and any summary info (e.g., how many attempts exist).

---

## Live APIs

*(controllers under `endpoints/rest/live/`)*

### 1. **SessionAPIs** (`/Session`)

* **GET /Session/Types**
  Returns the list of all session types available (e.g., “Practice,” “Revision,” “Mock Test”). Each type usually has an ID/name and default time allocation.

* **POST /Session/StartSession**
  Starts a brand‐new practice session for the current user. The request body contains fields such as userId, syllabusId, topicId, and any session preferences. Responds with the newly created session ID and initial metadata (start time, session type).

* **POST /Session/{sessionId}/EndSession**
  Ends (closes) the session identified by `sessionId`. The payload often includes total duration or any summary metrics. Once closed, no further problem‐attempt operations are allowed for this session.

* **POST /Session/StartProblemAttempt**
  Marks the start of an attempt on a single problem within the active session. Body contains `sessionId`, `problemId`, and timestamp. Returns an `attemptId`.

* **POST /Session/EndProblemAttempt**
  Marks the end of the current problem attempt. Body contains `attemptId`, the user’s answer, flags for correctness, time taken, etc. Records the result to the database.

* **POST /Session/StartPause**
  Indicates that the user is pausing the ongoing session (e.g., for a break). Payload: `sessionId` and pause‐start timestamp.

* **POST /Session/EndPause**
  Marks the end of a pause period (i.e., user resumes study). Payload: `sessionId` and pause‐end timestamp.

* **POST /Session/ExtendSession**
  Extends the time limit or expected duration of the session. Body typically has `sessionId` and extra minutes/seconds to append to the session.

* **GET /Session/{sessionId}/ActiveProblems**
  Retrieves all problems currently “in play” within the given session (i.e., problems assigned but not yet completed or pigeon‐marked).

* **GET /Session/{sessionId}/PigeonedProblems**
  Retrieves all problems that the user has marked as “pigeon” (postponed for later review) within this session.

---

### 2. **ActiveTopicAPIs** (`/Topic`)

* **GET /Topic/{topicId}/ActiveProblems**
  Returns all “active” (currently assigned or in‐progress) problems for the specified topic—across all sessions or for a particular user context. Useful for showing “problems to solve next” in a UI.

---

### 3. **AttemptedProblemAPIs** (`/Problem`)

* **GET /Problem/Pigeons**
  Returns the full list of problems that have ever been pigeon‐marked (i.e., flagged to revisit) by the current user. Each entry includes problemId, last attempt timestamp, and any associated notes.

* **GET /Problem/{problemId}**
  Fetches detailed metadata for a single problem (question text, answer key, difficulty, any hints).

* **GET /Problem/{problemId}/Attempts**
  Retrieves all past attempt records for the given `problemId` (timestamps, correctness, time taken, and so on).

* **DELETE /Problem/Attempt/{problemAttemptId}**
  Deletes the specified problem‐attempt record (e.g., removing an accidental attempt entry).

* **POST /Problem/ChangeState**
  Changes the “state” of a problem for the user (e.g., marks it Correct, Incorrect, Later, Pigeon, or Redo). The request body must include `problemId` and the new state code (e.g., `"Correct"` or `"Pigeon"`).

---

### 4. **AtomFeedController**

* **GET /event-feed.xml**
  Produces an Atom (Atom 1.0)-formatted XML document containing a time-ordered list of recent application events (e.g., session starts, problems solved). Returns `Content-Type: application/atom+xml`. This is the endpoint you’d hook into for any RSS/Atom readers.

---

## (Non-REST) Web & WebSocket Endpoints

#### SConsoleWebController (`endpoints/web/SConsoleWebController.java`)

* Defines a Spring-managed `@Bean` of type `CorsFilter` that applies a global CORS policy (`☐` allow all origins/methods/headers).
* **No HTTP resource-mapping methods** (no `@GetMapping`/`@PostMapping` here). It does *not* expose any REST URLs itself; it solely configures CORS for the full application.

#### AppRemoteWSController (`endpoints/websockets/controlscreen/AppRemoteWSController.java`)

* Annotated as a WebSocket (e.g., `@ServerEndpoint` or `@Controller` for SockJS/Stomp, depending on the code inside).
* Handles real-time messages from a remote control UI (e.g., to switch screens or push commands from a browser to the Swing client).
* **No standard HTTP-based endpoints**—all communication happens over a WebSocket subprotocol (ports/paths defined inside that class).

---

**Summary**

* **Master APIs** (under `/Master/…`): manage books, chapters, topics, problem metadata, syllabi, and tracking configurations.
* **Live APIs** (under `/Session`, `/Topic`, `/Problem`, plus `/event‐feed.xml`): handle session creation/end, problem attempts, state changes, and retrieving real-time feeds.
* **Web & WebSocket classes** provide CORS configuration and a remote-control channel but do not themselves expose REST endpoints.

You can use this as a reference when calling these endpoints from your frontend (Angular/UI) or for writing integration tests.
