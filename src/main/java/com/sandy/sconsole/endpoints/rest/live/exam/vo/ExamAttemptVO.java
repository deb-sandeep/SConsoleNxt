package com.sandy.sconsole.endpoints.rest.live.exam.vo;

import com.sandy.sconsole.dao.exam.ExamAttempt;
import com.sandy.sconsole.dao.exam.ExamSectionAttempt;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.ExamVO;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Comparator.comparingInt;

@Data
public class ExamAttemptVO {
    
    private Integer id ;
    private ExamVO  exam ;
    private Date    attemptDate ;
    private Integer score ;
    private Float   avoidableLossPct ;
    private Float   unavoidableLossPct ;
    private String  status ;
    
    private List<ExamSectionAttemptVO> sectionAttempts = new ArrayList<>() ;
    
    public ExamAttemptVO(){}
    
    public ExamAttemptVO( ExamAttempt entity ) {
        this.setId( entity.getId() ) ;
        this.setExam( new ExamVO( entity.getExam(), false ) ) ;
        this.setAttemptDate( Date.from( entity.getAttemptDate() ) ) ;
        this.setScore( entity.getScore() ) ;
        this.setAvoidableLossPct( entity.getAvoidableLossPct() ) ;
        this.setUnavoidableLossPct( entity.getUnavoidableLossPct() ) ;
        this.setStatus( entity.getStatus() ) ;
        
        for( ExamSectionAttempt sectionAttempt : entity.getSectionAttempts() ) {
            this.getSectionAttempts().add( new ExamSectionAttemptVO( sectionAttempt ) ) ;
        }
        
        sectionAttempts.sort( comparingInt( s -> s.getExamSection().getExamSequence() ) ) ;
    }
}
