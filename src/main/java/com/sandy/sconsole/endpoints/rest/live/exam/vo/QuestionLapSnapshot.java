package com.sandy.sconsole.endpoints.rest.live.exam.vo;

public record QuestionLapSnapshot(
    int examQuestionId,
    int timeSpentInCurrentLap,
    String attemptState
){}
