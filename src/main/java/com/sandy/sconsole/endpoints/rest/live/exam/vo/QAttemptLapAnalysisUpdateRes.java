package com.sandy.sconsole.endpoints.rest.live.exam.vo;

public record QAttemptLapAnalysisUpdateRes(
   int qAttemptId,
   String lapName,
   int attemptScore
) {}
