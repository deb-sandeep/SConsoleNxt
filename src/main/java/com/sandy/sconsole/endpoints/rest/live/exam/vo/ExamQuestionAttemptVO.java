package com.sandy.sconsole.endpoints.rest.live.exam.vo;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.dao.exam.ExamAttemptLapSnapshot;
import com.sandy.sconsole.dao.exam.ExamQuestionAttempt;
import com.sandy.sconsole.dao.exam.repo.ExamAttemptLapSnapshotRepo;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.ExamQuestionVO;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ExamQuestionAttemptVO {
    
    private Integer id ;
    private ExamQuestionVO examQuestion ;
    private Integer examSectionAttemptId ;
    private Integer timeSpent ;
    private String evaluationStatus ;
    private String answerProvided ;
    private String answerSubmitLap ;
    private String answerSubmitStatus ;
    private Integer score ;
    private Integer loss ;
    private Integer avoidableLoss ;
    private String rootCause ;
    private Map<String, Integer> lapDurations = new HashMap<>() ;
    
    public ExamQuestionAttemptVO(){}
    
    public ExamQuestionAttemptVO( ExamQuestionAttempt entity ) {
        this.setId( entity.getId() ) ;
        this.setExamQuestion( new ExamQuestionVO( entity.getExamQuestion() ) ) ;
        this.setExamSectionAttemptId( entity.getExamSectionAttempt().getId() ) ;
        this.setTimeSpent( entity.getTimeSpent() ) ;
        this.setEvaluationStatus( entity.getEvaluationStatus() ) ;
        this.setAnswerProvided( entity.getAnswerProvided() ) ;
        this.setAnswerSubmitLap( entity.getAnswerSubmitLap() ) ;
        this.setAnswerSubmitStatus( entity.getAnswerSubmitStatus() ) ;
        this.setScore( entity.getScore() ) ;
        this.setLoss( entity.getLoss() ) ;
        this.setAvoidableLoss( entity.getAvoidableLoss() ) ;
        this.setRootCause( entity.getRootCause() == null ? null : entity.getRootCause().getCause() ) ;
        
        populateLapDurations( entity ) ;
    }
    
    private void populateLapDurations( ExamQuestionAttempt attempt ) {
        
        ExamAttemptLapSnapshotRepo repo = SConsole.getBean( ExamAttemptLapSnapshotRepo.class ) ;
        List<ExamAttemptLapSnapshot> snapshots = repo.findByExamAttemptIdAndExamQuestionIdOrderByIdAsc(
                attempt.getExamSectionAttempt().getExamAttempt().getId(),
                attempt.getExamQuestion().getId() ) ;
        
        if( !snapshots.isEmpty() ) {
            for( ExamAttemptLapSnapshot snapshot : snapshots ) {
                lapDurations.put( snapshot.getLapName(), snapshot.getTimeSpent() ) ;
            }
        }
    }
}
