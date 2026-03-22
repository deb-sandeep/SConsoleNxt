package com.sandy.sconsole.endpoints.rest.live.vo;

public record AnswerUpdateReq(
   int questionAttemptId,
   String submitStatus,
   String answerProvided,
   int timeSpent
) {}
