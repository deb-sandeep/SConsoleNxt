package com.sandy.sconsole.endpoints.rest.live.tag.vo.reqres;

import com.sandy.sconsole.dao.master.TopicProblem;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.QuestionVO;

import java.util.List;

public record TagQuerySearchRes(
        List<TopicProblem> problems,
        List<QuestionVO> questions
) {}
