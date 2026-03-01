package com.sandy.sconsole.endpoints.rest.master.exam.vo.reqres;

import com.sandy.sconsole.endpoints.rest.master.exam.vo.QuestionVO;

import java.util.List;
import java.util.Map;

public record AvailableQuestionRes(
   int topicId,
   Map<String, List<QuestionVO>> questions
) {}
