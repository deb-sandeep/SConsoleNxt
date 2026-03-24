package com.sandy.sconsole.endpoints.rest.live.exam.vo;

public record AnswerUpdateReq(
   int questionAttemptId,
   String submitStatus,
   String answerProvided,
   String answerSubmitLap,
   int timeSpent
) {}
