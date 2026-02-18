package com.sandy.sconsole.endpoints.rest.master.vo;

import com.sandy.sconsole.dao.exam.Exam;
import com.sandy.sconsole.dao.exam.ExamSection;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class ExamVO {
    
    private Integer id ;
    private String type ;
    private String note ;
    private Integer numPhyQuestions ;
    private Integer numChemQuestions ;
    private Integer numMathQuestions ;
    private Integer totalMarks ;
    private Integer duration ;
    private Date creationDate ;
    private List<ExamSectionVO> sections = new ArrayList<>() ;
    
    public ExamVO(){}
    
    public ExamVO( Exam entity ) {
        this.setId( entity.getId() ) ;
        this.setType( entity.getType() ) ;
        this.setNote( entity.getNote() ) ;
        this.setNumPhyQuestions( entity.getNumPhyQuestions() ) ;
        this.setNumChemQuestions( entity.getNumChemQuestions() ) ;
        this.setNumMathQuestions( entity.getNumMathQuestions() ) ;
        this.setTotalMarks( entity.getTotalMarks() ) ;
        this.setDuration( entity.getDuration() ) ;
        this.setCreationDate( Date.from( entity.getCreationDate() ) ) ;
        
        for( ExamSection section : entity.getSections() ) {
            this.getSections().add( new ExamSectionVO( section ) ) ;
        }
    }
}
