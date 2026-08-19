package com.sandy.sconsole.endpoints.rest.master.tag.vo.reqres;

import com.sandy.sconsole.endpoints.rest.master.core.vo.ProblemVO;
import com.sandy.sconsole.endpoints.rest.master.tag.vo.QuestionSummaryVO;

import java.util.List;

public record TagAssociationRes( List<ProblemVO> problems, List<QuestionSummaryVO> questions ) {}
