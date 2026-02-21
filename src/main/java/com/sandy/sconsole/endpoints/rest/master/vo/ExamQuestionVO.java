package com.sandy.sconsole.endpoints.rest.master.vo;

import com.sandy.sconsole.dao.exam.ExamQuestion;
import lombok.Data;

@Data
public class ExamQuestionVO {
    
    private Integer id ;
    private Integer sequence ;
    private Integer questionId ;
    private Integer sectionId ;
    private QuestionVO question ;
    
    public ExamQuestionVO(){}
    
    public ExamQuestionVO( ExamQuestion entity ) {
        
        this.setId( entity.getId() ) ;
        this.setQuestionId( entity.getQuestion().getId() ) ;
        this.setQuestion( new QuestionVO( entity.getQuestion() ) ) ;
        this.setSectionId( entity.getSection().getId() ) ;
        this.setSequence( entity.getSequence() ) ;
    }
}
