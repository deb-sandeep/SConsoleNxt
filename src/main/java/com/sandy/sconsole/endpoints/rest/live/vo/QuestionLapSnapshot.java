package com.sandy.sconsole.endpoints.rest.live.vo;

public record QuestionLapSnapshot(
    int examQuestionId,
    int timeSpentInCurrentLap,
    String attemptState
){}
