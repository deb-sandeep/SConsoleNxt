package com.sandy.sconsole.endpoints.rest.live.exam.vo;

public record QuestionAttemptUpdateReq(
   int questionAttemptId,
   String submitStatus,
   String answerProvided,
   String answerSubmitLap,
   int timeSpent
) {}
