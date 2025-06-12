package com.sandy.sconsole.endpoints.rest.live;

import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.dao.master.TopicProblem;
import com.sandy.sconsole.dao.master.repo.ProblemRepo;
import com.sandy.sconsole.dao.master.repo.TopicProblemRepo;
import com.sandy.sconsole.dao.master.repo.TopicRepo;
import com.sandy.sconsole.dao.session.ProblemAttempt;
import com.sandy.sconsole.dao.session.dto.ProblemAttemptDTO;
import com.sandy.sconsole.dao.session.repo.ProblemAttemptRepo;
import com.sandy.sconsole.dao.session.repo.SessionRepo;
import com.sandy.sconsole.state.manager.ActiveTopicStatisticsManager;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

import static com.sandy.sconsole.EventCatalog.PROBLEM_ATTEMPT_ENDED;
import static com.sandy.sconsole.core.api.AR.success;
import static com.sandy.sconsole.core.api.AR.systemError;

@Slf4j
@RestController
@RequestMapping( "/Problem" )
public class AttemptedProblemAPIs {
    
    @Autowired
    private TopicProblemRepo tpRepo ;
    
    @Autowired
    private ProblemAttemptRepo paRepo ;
    
    @Autowired
    private ProblemRepo problemRepo ;
    
    @Autowired
    private TopicRepo topicRepo ;
    
    @Autowired
    private SessionRepo sessionRepo ;
    
    @Autowired
    private ActiveTopicStatisticsManager activeTopicStatsMgr ;
    
    @Autowired
    private EventBus eventBus ;
    
    @Autowired
    private TopicProblemRepo topicProblemRepo;
    
    @Autowired
    private PlatformTransactionManager txnMgr ;
    
    private TransactionTemplate txTemplate ;
    @Autowired
    private ProblemAttemptRepo  problemAttemptRepo;
    
    @PostConstruct
    public void init() {
        this.txTemplate = new TransactionTemplate( txnMgr ) ;
    }
    
    @GetMapping( "/Pigeons" )
    public ResponseEntity<AR<List<TopicProblem>>> getAllPigeonedProblems() {
        try {
            return success( tpRepo.findAllPigeonedProblems() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @GetMapping( "/{problemId}" )
    public ResponseEntity<AR<TopicProblem>> getTopicProblem(
            @PathVariable( "problemId" ) final int problemId ){
        try {
            return success( tpRepo.findById( problemId ).get() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    @GetMapping( "/{problemId}/Attempts" )
    public ResponseEntity<AR<List<ProblemAttemptDTO>>> getProblemAttempts(
            @PathVariable( "problemId" ) final int problemId ) {
        try {
            return success( paRepo.getProblemAttempts( problemId )
                                  .stream()
                                  .map(ProblemAttemptDTO::new)
                                  .toList() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @DeleteMapping( "/Attempt/{problemAttemptId}" )
    public ResponseEntity<AR<String>> deleteProblemAttempt(
            @PathVariable( "problemAttemptId" ) final int problemAttemptId ) {
        try {
            paRepo.deleteById( problemAttemptId ) ;
            return success() ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @Data
    public static class ProblemChangeStateRequest {
        private int[] problemIds ;
        private int topicId ;
        private String targetState ;
    }
    
    @PostMapping( "/ChangeState" )
    public ResponseEntity<AR<String>> changeProblemState( @RequestBody final ProblemChangeStateRequest req ) {
        
        try {
            for( int problemId : req.getProblemIds() ) {
                ProblemAttemptDTO dto = saveProblemAttempt( problemId, req ) ;
                eventBus.publishEvent( PROBLEM_ATTEMPT_ENDED, dto ) ;
            }
            return success() ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    protected ProblemAttemptDTO saveProblemAttempt( int problemId, ProblemChangeStateRequest req ) {
        
        return this.txTemplate.execute( status -> {
            
            ProblemAttempt pa = new ProblemAttempt() ;
            pa.setProblem( problemRepo.findById( problemId ).get() ) ;
            pa.setTopic( topicRepo.findById( req.getTopicId() ).get() ) ;
            pa.setPrevState( topicProblemRepo.findByProblemId( problemId ).getProblemState() ) ;
            pa.setTargetState( req.getTargetState() ) ;
            pa.setStartTime( new Date() ) ;
            pa.setEndTime( pa.getStartTime() ) ;
            pa.setEffectiveDuration( 0 ) ;
            
            // Session 0 is a special session to imply offline work by coach.
            pa.setSession( sessionRepo.findById( 0 ).get() ) ;
            
            ProblemAttempt savedDao = paRepo.save( pa ) ;
            return new ProblemAttemptDTO( savedDao ) ;
        } ) ;
    }
    
    @GetMapping( "/State/{bookId}/{chapterNum}" )
    public ResponseEntity<AR<List<ProblemAttemptRepo.ProblemState>>> getProblemStates(
            @PathVariable( "bookId" ) int bookId,
            @PathVariable( "chapterNum" ) int chapterNum ) {
        
        try {
            return success( problemAttemptRepo.getProblemState( bookId, chapterNum ) ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
}
