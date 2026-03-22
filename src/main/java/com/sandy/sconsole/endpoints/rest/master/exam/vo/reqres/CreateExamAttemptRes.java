package com.sandy.sconsole.endpoints.rest.master.exam.vo.reqres;

import java.util.Map;

public record CreateExamAttemptRes(
    int examId,
    int examAttemptId,
    // key: exam_question_id, value: exam_question_attempt_id
    Map<Integer, Integer> questionAttemptIds
){}
