package com.sandy.sconsole.endpoints.rest.live.exam.vo;

public record QAttemptLapAnalysisUpdateReq(
   int qAttemptLapAnalysisId,
   int qAttemptId,
   String lapName,
   int score,
   String note,
   String[] observations
) {}
