package com.sandy.sconsole.endpoints.rest.live.vo;

import java.util.List;

public record LapSnapshotReq(
    int examAttemptId,
    String currentLap,
    List<QuestionLapSnapshot> snapshots
) {}
