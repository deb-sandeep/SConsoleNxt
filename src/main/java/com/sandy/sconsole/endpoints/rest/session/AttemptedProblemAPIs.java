package com.sandy.sconsole.endpoints.rest.session;

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
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    
    
    @GetMapping( "/Pigeons" )
    public ResponseEntity<AR<List<TopicProblem>>> getAllPigeonedProblems() {
        try {
            return success( tpRepo.findAllPigeonedProblems() ) ;
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
    
    @Data
    public static class PigeonChangeStateRequest {
        private int problemId ;
        private int topicId ;
        private String currentState ;
        private String targetState ;
    }
    
    @PostMapping( "/Pigeon/ChangeState" )
    public ResponseEntity<AR<String>> changePigeonState( @RequestBody final PigeonChangeStateRequest req ) {
        
        try {
            ProblemAttempt pa = new ProblemAttempt() ;
            pa.setProblem( problemRepo.findById( req.getProblemId() ).get() ) ;
            pa.setTopic( topicRepo.findById( req.getTopicId() ).get() ) ;
            pa.setPrevState( req.getCurrentState() ) ;
            pa.setTargetState( req.getTargetState() ) ;
            pa.setStartTime( new Date() ) ;
            pa.setEndTime( pa.getStartTime() ) ;
            pa.setEffectiveDuration( 0 ) ;
            
            // Session 0 is a special session to imply offline work by coach.
            pa.setSession( sessionRepo.findById( 0 ).get() ) ;
            
            ProblemAttempt savedDao = paRepo.save( pa ) ;
            
            ProblemAttemptDTO dto = new ProblemAttemptDTO( savedDao ) ;
            activeTopicStatsMgr.handleProblemAttemptEnded( dto.getTopicId() ) ;
            eventBus.publishEvent( PROBLEM_ATTEMPT_ENDED, dto ) ;

            return success() ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
}
