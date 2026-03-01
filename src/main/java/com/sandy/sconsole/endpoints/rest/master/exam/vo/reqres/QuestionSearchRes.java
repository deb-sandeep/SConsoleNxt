package com.sandy.sconsole.endpoints.rest.master.exam.vo.reqres;

import com.sandy.sconsole.dao.exam.Question;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.QuestionVO;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public record QuestionSearchRes(
   long totalResults,
   int totalPages,
   int pageNumber,
   int pageSize,
   int resultsInPage,
   List<QuestionVO> questions
) {
    public static QuestionSearchRes from( Page<Question> dbSearchResult ) {
        
        List<QuestionVO> questions = new ArrayList<>() ;
        for( Question question : dbSearchResult.getContent() ) {
            QuestionVO questionVO = new QuestionVO( question ) ;
            questions.add( questionVO ) ;
        }
        
        return new QuestionSearchRes(
                dbSearchResult.getTotalElements(),
                dbSearchResult.getTotalPages(),
                dbSearchResult.getNumber(),
                dbSearchResult.getSize(),
                dbSearchResult.getNumberOfElements(),
                questions
        ) ;
    }
}
