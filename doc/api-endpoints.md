**Master APIs**

* **BookAPIs** (`/Master/Book`)

    * GET `/Master/Book/Listing`
    * GET `/Master/Book/{bookId}/ProblemSummary`
    * GET `/Master/Book/TopicMappings`
    * POST `/Master/Book/{bookId}/UpdateAttribute`
    * POST `/Master/Book/{bookId}/{chapterNum}/SaveChapterName`
    * POST `/Master/Book/{bookId}/{chapterNum}/{exerciseNum}/UpdateExerciseName`

* **BookUploadAPIs** (`/Master/Book`)

    * POST `/Master/Book/ValidateMetaFile`
    * POST `/Master/Book/SaveMeta`
    * POST `/Master/Book/CreateNewExercise`

* **ChapterTopicMappingAPIs** (`/Master/ChapterTopicMapping`)

    * GET `/Master/ChapterTopicMapping`
    * POST `/Master/ChapterTopicMapping`
    * DELETE `/Master/ChapterTopicMapping/{mapId}`
    * POST `/Master/ChapterTopicMapping/SwapAttemptSequence/{mappingId1}/{mappingId2}`
    * POST `/Master/ChapterTopicMapping/ToggleProblemMappingDone/{mappingId}`

* **ProblemTopicMappingAPIs** (`/Master/ProblemTopicMapping`)

    * GET `/Master/ProblemTopicMapping/Book/{bookId}/Chapter/{chapterNum}`
    * POST `/Master/ProblemTopicMapping/AttachProblems/{topicChapterMapId}`
    * POST `/Master/ProblemTopicMapping/DetachProblems`

* **ProblemAPIs** (`/Master/Problem`)

    * POST `/Master/Problem/{problemId}/DifficultyLevel/{difficultyLevel}`

* **SyllabusAPIs** (`/Master/Syllabus`)

    * GET `/Master/Syllabus/All`

* **TrackAPIs** (`/Master/Track`)

    * GET `/Master/Track/All`
    * GET `/Master/Track/CurrentTopicAssignments`
    * POST `/Master/Track/{id}/SaveTopicSchedules`

* **TopicAPIs** (`/Master/Topic`)

    * GET `/Master/Topic/{topicId}`
    * GET `/Master/Topic/All`
    * GET `/Master/Topic/ProblemTypeCounts`
    * GET `/Master/Topic/{topicId}/Problems`

---

**Live APIs**

* **SessionAPIs** (`/Session`)

    * GET `/Session/Types`
    * POST `/Session/StartSession`
    * POST `/Session/{sessionId}/EndSession`
    * POST `/Session/StartProblemAttempt`
    * POST `/Session/EndProblemAttempt`
    * POST `/Session/StartPause`
    * POST `/Session/EndPause`
    * POST `/Session/ExtendSession`
    * GET `/Session/{sessionId}/ActiveProblems`
    * GET `/Session/{sessionId}/PigeonedProblems`

* **ActiveTopicAPIs** (`/Topic`)

    * GET `/Topic/{topicId}/ActiveProblems`

* **AttemptedProblemAPIs** (`/Problem`)

    * GET `/Problem/Pigeons`
    * GET `/Problem/{problemId}`
    * GET `/Problem/{problemId}/Attempts`
    * DELETE `/Problem/Attempt/{problemAttemptId}`
    * POST `/Problem/ChangeState`

* **AtomFeedController**

    * GET `/event-feed.xml`
