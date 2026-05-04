package com.sandy.sconsole.endpoints.rest.live.exam.vo;

public record QAttemptLapAnalysisUpdateReq(
   String lapName,
   int score,
   String note,
   String[] observations
) {}
