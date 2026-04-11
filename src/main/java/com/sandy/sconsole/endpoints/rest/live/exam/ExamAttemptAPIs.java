package com.sandy.sconsole.endpoints.rest.live.exam;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.exam.ExamAttempt;
import com.sandy.sconsole.dao.exam.ExamEventLog;
import com.sandy.sconsole.dao.exam.ExamQuestionAttempt;
import com.sandy.sconsole.dao.exam.ExamQuestionAttemptRepo;
import com.sandy.sconsole.dao.exam.repo.ExamAttemptRepo;
import com.sandy.sconsole.dao.exam.repo.ExamEventLogRepo;
import com.sandy.sconsole.endpoints.rest.live.exam.helper.ExamAttemptHelper;
import com.sandy.sconsole.endpoints.rest.live.exam.helper.ExamEvaluationHelper;
import com.sandy.sconsole.endpoints.rest.live.exam.vo.ExamAttemptVO;
import com.sandy.sconsole.endpoints.rest.live.exam.vo.ExamEventVO;
import com.sandy.sconsole.endpoints.rest.live.exam.vo.LapSnapshotReq;
import com.sandy.sconsole.endpoints.rest.live.exam.vo.QuestionAttemptUpdateReq;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.reqres.CreateExamAttemptRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.sandy.sconsole.core.api.AR.systemError;

@Slf4j
@RestController
@RequestMapping( "/Exam" )
public class ExamAttemptAPIs {
    
    @Autowired
    private ExamAttemptRepo attemptRepo = null ;
    
    @Autowired
    private ExamEventLogRepo eventLogRepo = null ;
    
    @Autowired
    private ExamQuestionAttemptRepo eqaRepo = null ;
    
    @Autowired
    private JdbcTemplate jdbcTemplate = null ;
    
    @GetMapping( "/Attempts" )
    public ResponseEntity<AR<List<ExamAttemptVO>>> getExamAttempts() {
        
        try {
            List<ExamAttemptVO> res = new ArrayList<>() ;
            for( ExamAttempt attempt : attemptRepo.findAll() ) {
                res.add( new ExamAttemptVO( attempt, null, false ) ) ;
            }
            res.sort( Comparator.comparing( ExamAttemptVO::getAttemptDate ).reversed() ) ;
            return AR.success( res ) ;
        }
        catch( IllegalArgumentException e ) {
            return AR.badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @GetMapping( "/Attempt/{examAttemptId}" )
    public ResponseEntity<AR<ExamAttemptVO>> getExamAttempt( @PathVariable int examAttemptId ) {
        try {
            ExamEvaluationHelper helper = SConsole.getBean( ExamEvaluationHelper.class ) ;
            ExamAttemptVO attempt = helper.getExamAttempt( examAttemptId ) ;
            return AR.success( attempt ) ;
        }
        catch( IllegalArgumentException e ) {
            return AR.badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @PostMapping( "/{examId}/Attempt" )
    @Transactional
    public ResponseEntity<AR<CreateExamAttemptRes>> createExamAttempt(
            @PathVariable int examId ) {
        
        try {
            ExamAttemptHelper helper = SConsole.getBean( ExamAttemptHelper.class ) ;
            CreateExamAttemptRes res = helper.createExamAttempt( examId ) ;
            return AR.success( res ) ;
        }
        catch( IllegalArgumentException e ) {
            return AR.badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @PostMapping( "/EventLog" )
    @Transactional
    public ResponseEntity<AR<String>> eventLog( @RequestBody ExamEventVO vo ) {
        
        try {
            ExamAttempt examAttempt = attemptRepo.findById( vo.getExamAttemptId() ).get() ;
            
            ExamEventLog event = new ExamEventLog() ;
            event.setExamAttempt( examAttempt ) ;
            event.setSequence( vo.getSequence() ) ;
            event.setEventType( vo.getEventType() ) ;
            event.setEventName( vo.getEventName() ) ;
            event.setPayload( vo.getPayload() ) ;
            event.setCreationTime( vo.getCreationTime() ) ;
            event.setTimeMarker( vo.getTimeMarker() ) ;
            
            eventLogRepo.save( event ) ;
            
            return AR.success() ;
        }
        catch( IllegalArgumentException e ) {
            return AR.badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @PostMapping( "/TimeUpdate" )
    @Transactional
    public ResponseEntity<AR<String>> updateTimeSpent(
            @RequestBody QuestionAttemptUpdateReq req ) {
        
        try {
            ExamQuestionAttempt eqa = eqaRepo.findByIdForUpdate( req.questionAttemptId() ).get() ;
            eqa.setTimeSpent( Math.max( eqa.getTimeSpent(), req.timeSpent() ) ) ;
            eqaRepo.save( eqa ) ;
            return AR.success() ;
        }
        catch( IllegalArgumentException e ) {
            return AR.badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @PostMapping( "/AnswerUpdate" )
    @Transactional
    public ResponseEntity<AR<String>> updateAnswerStatus(
            @RequestBody QuestionAttemptUpdateReq req ) {
        
        try {
            ExamQuestionAttempt eqa = eqaRepo.findByIdForUpdate( req.questionAttemptId() ).get() ;

            eqa.setAnswerSubmitStatus( req.submitStatus() ) ;
            eqa.setAnswerProvided( req.answerProvided() ) ;
            eqa.setAnswerSubmitLap( req.answerSubmitLap() ) ;
            eqa.setTimeSpent( Math.max( eqa.getTimeSpent(), req.timeSpent() ) ) ;
            
            eqaRepo.save( eqa ) ;
            return AR.success() ;
        }
        catch( IllegalArgumentException e ) {
            return AR.badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @PostMapping( "/LapSnapshot" )
    @Transactional
    public ResponseEntity<AR<String>> saveLapSnapshot(
            @RequestBody LapSnapshotReq req ) {
        
        try {
            jdbcTemplate.batchUpdate(
                """
                insert into exam_attempt_lap_snapshot
                ( exam_attempt_id, exam_question_id, lap_name, time_spent, attempt_status )
                values ( ?, ?, ?, ?, ? )
                """,
                req.snapshots(),
                100,
                ( ps, snapshot ) -> {
                    ps.setInt(    1, req.examAttemptId() ) ;
                    ps.setInt(    2, snapshot.examQuestionId() ) ;
                    ps.setString( 3, req.currentLap() ) ;
                    ps.setInt(    4, snapshot.timeSpentInCurrentLap() ) ;
                    ps.setString( 5, snapshot.attemptState() ) ;
                }
            ) ;
            return AR.success() ;
        }
        catch( IllegalArgumentException e ) {
            return AR.badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @PostMapping( "/{examAttemptId}/Submit" )
    @Transactional
    public ResponseEntity<AR<ExamAttemptVO>> submitExamAttempt( @PathVariable int examAttemptId ) {
        
        try {
            ExamEvaluationHelper helper = SConsole.getBean( ExamEvaluationHelper.class ) ;
            
            ExamAttemptVO res = helper.evaluateExamAttempt( examAttemptId ) ;
            return AR.success( res ) ;
        }
        catch( IllegalArgumentException e ) {
            return AR.badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @PostMapping( "/RootCauseUpdate/{qAttemptId}/{rootCause}" )
    public ResponseEntity<AR<ExamAttemptVO>> updateRootCause(
            @PathVariable Integer qAttemptId,
            @PathVariable String rootCause ) {
        
        try {
            ExamEvaluationHelper helper = SConsole.getBean( ExamEvaluationHelper.class ) ;
            ExamAttemptVO res = helper.updateQuestionAttemptRootCause( qAttemptId, rootCause ) ;
            return AR.success( res ) ;
        }
        catch( IllegalArgumentException e ) {
            return AR.badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @PostMapping( "/ScoreOverride/{qAttemptId}/{score}" )
    public ResponseEntity<AR<ExamAttemptVO>> overrideScore(
            @PathVariable Integer qAttemptId,
            @PathVariable Integer score ) {
        try {
            ExamEvaluationHelper helper = SConsole.getBean( ExamEvaluationHelper.class ) ;
            ExamAttemptVO res = helper.overrideScore( qAttemptId, score ) ;
            return AR.success( res ) ;
        }
        catch( IllegalArgumentException e ) {
            return AR.badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
}
