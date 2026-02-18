package com.sandy.sconsole.endpoints.rest.master.vo;

import com.sandy.sconsole.dao.exam.ExamQuestion;
import com.sandy.sconsole.dao.exam.ExamSection;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ExamSectionVO {
    
    private Integer id ;
    private String syllabusName ;
    private String problemType ;
    private Integer examId ;
    private String title ;
    private Integer correctMarks ;
    private Integer examSequence ;
    private Integer wrongPenalty ;
    private Integer numQuestions ;
    private Integer numCompulsoryQuestions ;
    private String instructions ;
    private List<ExamQuestionVO> questions = new ArrayList<>() ;
    
    public ExamSectionVO(){}
    
    public ExamSectionVO( ExamSection entity ) {
        this.setId( entity.getId() ) ;
        this.setSyllabusName( entity.getSyllabusName().getSyllabusName() ) ;
        this.setProblemType( entity.getProblemType().getProblemType() ) ;
        this.setExamId( entity.getExam().getId() ) ;
        this.setTitle( entity.getTitle() ) ;
        this.setCorrectMarks( entity.getCorrectMarks() ) ;
        this.setExamSequence( entity.getExamSequence() ) ;
        this.setWrongPenalty( entity.getWrongPenalty() ) ;
        this.setNumQuestions( entity.getNumQuestions() ) ;
        this.setNumCompulsoryQuestions( entity.getNumCompulsoryQuestions() ) ;
        this.setInstructions( entity.getInstructions() ) ;
        
        for( ExamQuestion question : entity.getQuestions() ) {
            this.getQuestions().add( new ExamQuestionVO( question ) ) ;
        }
    }
}
