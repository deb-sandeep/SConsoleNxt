package com.sandy.sconsole.endpoints.rest.live.exam.vo;

import com.sandy.sconsole.dao.exam.ExamQuestionAttempt;
import com.sandy.sconsole.dao.exam.ExamSectionAttempt;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.ExamSectionVO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import static java.util.Comparator.comparingInt;

@Data
public class ExamSectionAttemptVO {
    
    private Integer       id ;
    private ExamSectionVO examSection ;
    private Integer       examAttemptId ;
    private Integer score ;
    private Float avoidableLossPct ;
    private Float unavoidableLossPct ;
    private List<ExamQuestionAttemptVO> questionAttempts = new ArrayList<>() ;
    
    public ExamSectionAttemptVO(){}
    
    public ExamSectionAttemptVO( ExamSectionAttempt entity ) {
        this.setId( entity.getId() ) ;
        this.setExamSection( new ExamSectionVO( entity.getExamSection(), false ) ) ;
        this.setExamAttemptId( entity.getExamAttempt().getId() ) ;
        this.setScore( entity.getScore() ) ;
        this.setAvoidableLossPct( entity.getAvoidableLossPct() ) ;
        this.setUnavoidableLossPct( entity.getUnavoidableLossPct() ) ;
        
        for( ExamQuestionAttempt questionAttempt : entity.getQuestionAttempts() ) {
            this.getQuestionAttempts().add( new ExamQuestionAttemptVO( questionAttempt ) ) ;
        }
        
        questionAttempts.sort( comparingInt( q -> q.getExamQuestion().getSequence() ) ) ;
    }
}
