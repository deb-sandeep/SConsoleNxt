package com.sandy.sconsole.endpoints.rest.master.tag.vo;

import com.sandy.sconsole.dao.exam.Question;
import lombok.Data;

@Data
public class QuestionSummaryVO {

    private int    id ;
    private String questionId ;
    private String topicName ;
    private String problemType ;
    private int    questionNumber ;
    
    public QuestionSummaryVO( Question q ) {
        this.id = q.getId() ;
        this.questionId = q.getQuestionId() ;
        this.topicName = q.getTopic().getTopicName() ;
        this.problemType = q.getProblemType().getProblemType() ;
        this.questionNumber = q.getQuestionNumber() ;
    }
}
