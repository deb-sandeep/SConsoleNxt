package com.sandy.sconsole.endpoints.rest.live;

import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.ui.screen.ScreenManager;
import com.sandy.sconsole.dao.master.SessionType;
import com.sandy.sconsole.dao.master.TopicProblem;
import com.sandy.sconsole.dao.master.repo.ProblemRepo;
import com.sandy.sconsole.dao.master.repo.SessionTypeRepo;
import com.sandy.sconsole.dao.master.repo.TopicProblemRepo;
import com.sandy.sconsole.dao.master.repo.TopicRepo;
import com.sandy.sconsole.dao.session.ProblemAttempt;
import com.sandy.sconsole.dao.session.Session;
import com.sandy.sconsole.dao.session.SessionPause;
import com.sandy.sconsole.dao.session.dto.ProblemAttemptDTO;
import com.sandy.sconsole.dao.session.dto.SessionDTO;
import com.sandy.sconsole.dao.session.dto.SessionPauseDTO;
import com.sandy.sconsole.dao.session.repo.ProblemAttemptRepo;
import com.sandy.sconsole.dao.session.repo.SessionPauseRepo;
import com.sandy.sconsole.dao.session.repo.SessionRepo;
import com.sandy.sconsole.ui.screen.session.SessionScreen;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sandy.sconsole.EventCatalog.*;
import static com.sandy.sconsole.core.api.AR.*;

@Slf4j
@RestController
@RequestMapping( "/Session" )
public class SessionAPIs {
    
    @Autowired private SessionTypeRepo    stRepo ;
    @Autowired private SessionRepo        sessionRepo ;
    @Autowired private SessionPauseRepo   sessionPauseRepo ;
    @Autowired private TopicRepo          topicRepo ;
    @Autowired private TopicProblemRepo   tpRepo ;
    @Autowired private ProblemRepo        problemRepo ;
    @Autowired private ProblemAttemptRepo paRepo ;
    
    @Autowired private ScreenManager screenManager ;
    
    @Autowired private EventBus eventBus ;
    
    @Autowired private PlatformTransactionManager txnMgr ;
    
    private TransactionTemplate txTemplate ;
    
    @PostConstruct
    public void init() {
        this.txTemplate = new TransactionTemplate( txnMgr ) ;
    }
    
    @GetMapping( "/Types" )
    public ResponseEntity<AR<List<SessionType>>> getAllSessionTypes() {
        try {
            return success( stRepo.findAll() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @PostMapping( "/StartSession" )
    public ResponseEntity<AR<Integer>> startSession( @RequestBody SessionDTO req ) {
        try {
            Session savedDao = this.txTemplate.execute( status -> {
                Session dao = new Session() ;
                dao.setSessionType( req.getSessionType() ) ;
                dao.setTopic( topicRepo.findById( req.getTopicId() ).get() ) ;
                dao.setSyllabusName( req.getSyllabusName() ) ;
                dao.setStartTime( req.getStartTime() ) ;
                dao.setEndTime( dao.getStartTime() ) ;
                dao.setEffectiveDuration( req.getEffectiveDuration() ) ;
                
                return sessionRepo.save( dao ) ;
            } ) ;
            
            assert savedDao != null;
            SessionDTO sessionDto = new SessionDTO( savedDao ) ;
            
            eventBus.publishEvent( SESSION_STARTED, sessionDto ) ;
            
            screenManager.scheduleScreenChange( SessionScreen.ID ) ;
            
            return success( savedDao.getId() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @PostMapping( "/{sessionId}/EndSession" )
    public ResponseEntity<AR<String>> endSession(  @PathVariable( "sessionId" ) int sessionId ) {
        try {
            eventBus.publishEvent( SESSION_ENDED, sessionId ) ;
            screenManager.showRootScreen() ;
            return success() ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @PostMapping( "/StartProblemAttempt" )
    public ResponseEntity<AR<Map<String, Integer>>> startProblemAttempt( @RequestBody ProblemAttemptDTO req ) {
        try {
            
            Session session = sessionRepo.findById( req.getSessionId() ).get() ;
            ProblemAttempt pa = new ProblemAttempt() ;
            
            ProblemAttempt savedDao = this.txTemplate.execute( status -> {
                pa.setSession( session ) ;
                pa.setTopic( session.getTopic() );
                pa.setProblem( problemRepo.findById( req.getProblemId() ).get() ) ;
                pa.setStartTime( req.getStartTime() ) ;
                pa.setEndTime( req.getEndTime() ) ;
                pa.setEffectiveDuration( req.getEffectiveDuration() ) ;
                pa.setPrevState( req.getPrevState() ) ;
                pa.setTargetState( req.getTargetState() ) ;
                
                return paRepo.save( pa ) ;
            } ) ;
            assert savedDao != null;
            
            Integer totalAttemptTime = paRepo.getTotalAttemptTime( req.getProblemId() ) ;
            
            Map<String, Integer> response = new HashMap<>() ;
            response.put( "problemAttemptId", savedDao.getId() ) ;
            response.put( "totalDuration", totalAttemptTime == null ? 0 : totalAttemptTime ) ;
            
            eventBus.publishEvent( PROBLEM_ATTEMPT_STARTED, new ProblemAttemptDTO( savedDao ) ) ;

            return success( response ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @PostMapping( "/EndProblemAttempt" )
    public ResponseEntity<AR<String>> endProblemAttempt( @RequestBody ProblemAttemptDTO req ) {
        try {
            ProblemAttempt pa = paRepo.findById( req.getId() ).get() ;
            pa.setTargetState( req.getTargetState() ) ;
            
            ProblemAttempt savedDao = this.txTemplate.execute( s -> paRepo.save( pa ) ) ;
            assert savedDao != null;
            
            eventBus.publishEvent( PROBLEM_ATTEMPT_ENDED, new ProblemAttemptDTO( savedDao ) ) ;
            
            return success() ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @PostMapping( "/StartPause" )
    public ResponseEntity<AR<Integer>> createNewPause( @RequestBody SessionPauseDTO req ) {
        try {
            SessionPause dao = new SessionPause() ;
            dao.setSession( sessionRepo.findById( req.getSessionId() ).get() ) ;
            dao.setStartTime( req.getStartTime() ) ;
            dao.setEndTime( req.getStartTime() ) ;
            
            SessionPause savedDao = this.txTemplate.execute( s -> sessionPauseRepo.saveAndFlush( dao ) ) ;
            assert savedDao != null;
            
            eventBus.publishEvent( PAUSE_STARTED, new SessionPauseDTO( savedDao )  );
            
            return success( savedDao.getId() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @PostMapping( "/EndPause" )
    public ResponseEntity<AR<String>> endPause( @RequestBody SessionPauseDTO req ) {
        try {
            eventBus.publishEvent( PAUSE_ENDED, req );
            return success() ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @PostMapping( "/ExtendSession" )
    public ResponseEntity<AR<String>> extendSession( @RequestBody ExtendSessionReq req ) {
        try {
            if( sessionRepo.findById( req.getSessionId() ).isPresent() ) {
                
                SessionExtensionDTO extensionDTO = this.txTemplate.execute( status -> {
                    
                    Session dao = sessionRepo.findById( req.getSessionId() ).get() ;
                    dao.setEndTime( req.getEndTime() ) ;
                    dao.setEffectiveDuration( req.getSessionEffectiveDuration() ) ;
                    
                    SessionDTO sessionDto ;
                    SessionPauseDTO pauseDTO = null ;
                    ProblemAttemptDTO paDTO = null ;
                    
                    Session savedSessionDao = sessionRepo.save( dao ) ;
                    sessionDto = new SessionDTO( savedSessionDao ) ;
                    
                    if( req.getPauseId() > 0 ) {
                        SessionPause pauseDao = sessionPauseRepo.findById( req.getPauseId() ).get() ;
                        pauseDao.setEndTime( req.getEndTime() ) ;
                        
                        SessionPause savedSPDao = sessionPauseRepo.save( pauseDao ) ;
                        pauseDTO = new SessionPauseDTO( savedSPDao ) ;
                    }
                    
                    if( req.getProblemAttemptId() > 0 ) {
                        ProblemAttempt paDao = paRepo.findById( req.getProblemAttemptId() ).get() ;
                        paDao.setEndTime( req.getEndTime() ) ;
                        paDao.setEffectiveDuration( req.getProblemAttemptEffectiveDuration() ) ;
                        
                        ProblemAttempt savedPADao = paRepo.save( paDao ) ;
                        paDTO = new ProblemAttemptDTO( savedPADao ) ;
                    }

                    return new SessionExtensionDTO( sessionDto, pauseDTO, paDTO ) ;
                } ) ;
                
                eventBus.publishEvent( SESSION_EXTENDED, extensionDTO ) ;
                return success() ;
            }
            return success( "No active session" ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @GetMapping( "/{sessionId}/ActiveProblems" )
    public ResponseEntity<AR<List<TopicProblem>>> getActiveProblemsForTopic( @PathVariable( "sessionId" ) int sessionId ) {
        try {
            if( sessionRepo.findById( sessionId ).isPresent() ) {
                Session session = sessionRepo.findById( sessionId ).get() ;
                return success( tpRepo.findActiveProblems( session.getTopic().getId() )) ;
            }
            return functionalError( "No active session" ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    @GetMapping( "/{sessionId}/PigeonedProblems" )
    public ResponseEntity<AR<List<TopicProblem>>> getPigeonedProblemsForTopic( @PathVariable( "sessionId" ) int sessionId ) {
        try {
            if( sessionRepo.findById( sessionId ).isPresent() ) {
                Session session = sessionRepo.findById( sessionId ).get() ;
                return success( tpRepo.findPigeonedProblems( session.getTopic().getId() )) ;
            }
            return functionalError( "No active session" ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
}
