package com.sandy.sconsole.endpoints.rest.master.exam.vo;

import com.sandy.sconsole.dao.exam.ExamQuestion;
import com.sandy.sconsole.dao.exam.ExamSection;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ExamSectionVO {
    
    private Integer id ;
    private Integer examId ;
    private Integer examSequence ;
    private String syllabusName ;
    private String problemType ;
    private String title ;
    private Integer correctMarks ;
    private Integer wrongPenalty ;
    private Integer numQuestions ;
    private Integer numCompulsoryQuestions ;
    private List<String> instructions ;
    private List<ExamQuestionVO> questions = new ArrayList<>() ;
    
    public ExamSectionVO(){}
    
    public ExamSectionVO( ExamSection entity ) {
        this.setId( entity.getId() ) ;
        this.setSyllabusName( entity.getSyllabus().getSyllabusName() ) ;
        this.setProblemType( entity.getProblemType().getProblemType() ) ;
        this.setExamId( entity.getExam().getId() ) ;
        this.setTitle( entity.getTitle() ) ;
        this.setCorrectMarks( entity.getCorrectMarks() ) ;
        this.setExamSequence( entity.getExamSequence() ) ;
        this.setWrongPenalty( entity.getWrongPenalty() ) ;
        this.setNumQuestions( entity.getNumQuestions() ) ;
        this.setNumCompulsoryQuestions( entity.getNumCompulsoryQuestions() ) ;
        this.setInstructions( List.of( entity.getInstructions().split( "\n" ) ) ) ;
        
        for( ExamQuestion question : entity.getQuestions() ) {
            this.getQuestions().add( new ExamQuestionVO( question ) ) ;
        }
    }
}
