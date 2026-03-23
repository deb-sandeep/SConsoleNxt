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
import com.sandy.sconsole.endpoints.rest.live.exam.vo.AnswerUpdateReq;
import com.sandy.sconsole.endpoints.rest.live.exam.vo.ExamEventVO;
import com.sandy.sconsole.endpoints.rest.live.exam.vo.LapSnapshotReq;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.reqres.CreateExamAttemptRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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
            event.setEventId( vo.getEventId() ) ;
            event.setPayload( vo.getPayload() ) ;
            event.setCreationTime( vo.getCreationTime() ) ;
            event.setTimeMarker( vo.getTimeMarker() ) ;
            
            eventLogRepo.save( event ) ;
            log.debug( "Event Logged: {}", event.getEventId() );
            
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
            @RequestBody AnswerUpdateReq req ) {
        
        try {
            ExamQuestionAttempt eqa = eqaRepo.findById( req.questionAttemptId() ).get() ;

            eqa.setAnswerSubmitStatus( req.submitStatus() ) ;
            eqa.setAnswerProvided( req.answerProvided() ) ;
            eqa.setTimeSpent( req.timeSpent() ) ;
            
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
            log.debug( "Inserting lap snapshot" ) ;
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
    public ResponseEntity<AR<String>> submitExamAttempt( @PathVariable int examAttemptId ) {
        
        try {
            log.debug( "Submitting exam attempt {}", examAttemptId ) ;
            ExamEvaluationHelper helper = SConsole.getBean( ExamEvaluationHelper.class ) ;
            helper.evaluateExamAttempt( examAttemptId ) ;
            return AR.success() ;
        }
        catch( IllegalArgumentException e ) {
            return AR.badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
}
