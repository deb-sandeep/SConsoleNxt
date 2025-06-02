package com.sandy.sconsole.endpoints.rest.live;

import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.core.atomfeed.AtomFeedService;
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
import com.sandy.sconsole.state.manager.ActiveTopicStatisticsManager;
import com.sandy.sconsole.state.manager.TodaySessionStatistics;
import com.sandy.sconsole.ui.screen.session.SessionScreen;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sandy.sconsole.EventCatalog.*;
import static com.sandy.sconsole.core.api.AR.*;

@Slf4j
@RestController
@RequestMapping( "/Session" )
@Transactional
public class SessionAPIs {
    
    @Autowired private SessionTypeRepo    stRepo ;
    @Autowired private SessionRepo        sessionRepo ;
    @Autowired private SessionPauseRepo   sessionPauseRepo ;
    @Autowired private TopicRepo          topicRepo ;
    @Autowired private TopicProblemRepo   tpRepo ;
    @Autowired private ProblemRepo        problemRepo ;
    @Autowired private ProblemAttemptRepo paRepo ;
    
    @Autowired private ScreenManager screenManager ;
    
    @Autowired private TodaySessionStatistics todayStudyStats ;
    @Autowired private ActiveTopicStatisticsManager activeTopicStatsMgr ;
    @Autowired private EventBus eventBus ;
    @Autowired private AtomFeedService atomFeed ;
    
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
            Session dao = new Session() ;
            dao.setSessionType( req.getSessionType() ) ;
            dao.setTopic( topicRepo.findById( req.getTopicId() ).get() ) ;
            dao.setSyllabusName( req.getSyllabusName() ) ;
            dao.setStartTime( req.getStartTime() ) ;
            dao.setEndTime( dao.getStartTime() ) ;
            dao.setEffectiveDuration( req.getEffectiveDuration() ) ;
            
            Session savedDao = sessionRepo.saveAndFlush( dao ) ;
            SessionDTO sessionDto = new SessionDTO( savedDao ) ;
            
            eventBus.publishEvent( SESSION_STARTED, sessionDto ) ;
            
            atomFeed.addFeedEvent( "text", "Session Started: " + sessionDto.getSessionType(),
                                   "\n%s: %s\n%tc",
                                   sessionDto.getSyllabusName(),
                                   sessionDto.getTopicName(),
                                   sessionDto.getStartTime() ) ;
            
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
            
            Session session = sessionRepo.findById( sessionId ).get() ;
            
            eventBus.publishEvent( SESSION_ENDED, sessionId ) ;
            atomFeed.addFeedEvent( "text", "Session Ended: " + session.getSessionType(),
                    "\n%s: %s\nDuration: %d minutes\n%tc",
                    session.getSyllabusName(),
                    session.getTopic().getTopicName(),
                    session.getEffectiveDuration()/60,
                    session.getEndTime() ) ;

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
            pa.setSession( session ) ;
            pa.setTopic( session.getTopic() );
            pa.setProblem( problemRepo.findById( req.getProblemId() ).get() ) ;
            pa.setStartTime( req.getStartTime() ) ;
            pa.setEndTime( req.getEndTime() ) ;
            pa.setEffectiveDuration( req.getEffectiveDuration() ) ;
            pa.setPrevState( req.getPrevState() ) ;
            pa.setTargetState( req.getTargetState() ) ;
            
            ProblemAttempt savedDao = paRepo.saveAndFlush( pa ) ;
            
            Integer totalAttemptTime = paRepo.getTotalAttemptTime( req.getProblemId() ) ;
            
            Map<String, Integer> response = new HashMap<>() ;
            response.put( "problemAttemptId", savedDao.getId() ) ;
            response.put( "totalDuration", totalAttemptTime == null ? 0 : totalAttemptTime ) ;
            
            atomFeed.addFeedEvent( "text", "Problem Attempt Started",
                    "\n%s: %s\n%s\n%d. %s\n%s\n%tc",
                    session.getSyllabusName(),
                    session.getTopic().getTopicName(),
                    pa.getProblem().getChapter().getBook().getBookShortName(),
                    pa.getProblem().getChapter().getId().getChapterNum(),
                    pa.getProblem().getChapter().getChapterName(),
                    pa.getProblem().getProblemKey(),
                    pa.getStartTime() ) ;
            
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
            ProblemAttempt savedDao = paRepo.saveAndFlush( pa ) ;
            
            ProblemAttemptDTO dto = new ProblemAttemptDTO( savedDao ) ;
            eventBus.publishEvent( PROBLEM_ATTEMPT_ENDED, dto ) ;
            
            atomFeed.addFeedEvent( "text", "Problem Attempt Ended: " + pa.getTargetState(),
                    "\n%s\n%d minutes\n%tc",
                    pa.getProblem().getProblemKey(),
                    pa.getEffectiveDuration()/60,
                    pa.getEndTime() ) ;

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
            
            SessionPause savedDao = sessionPauseRepo.saveAndFlush( dao ) ;
            
            eventBus.publishEvent( PAUSE_STARTED, new SessionPauseDTO( savedDao )  );
            
            atomFeed.addFeedEvent( "text", "Pause Started", "\n%tc", savedDao.getStartTime() ) ;
            
            return success( savedDao.getId() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @PostMapping( "/EndPause" )
    public ResponseEntity<AR<String>> endPause( @RequestBody SessionPauseDTO req ) {
        try {
            eventBus.publishEvent( PAUSE_ENDED );
            
            atomFeed.addFeedEvent( "text", "Pause Ended", "\n%tc", new Date() ) ;
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
                Session dao = sessionRepo.findById( req.getSessionId() ).get() ;
                dao.setEndTime( req.getEndTime() ) ;
                dao.setEffectiveDuration( req.getSessionEffectiveDuration() ) ;
                
                Session savedSessionDao = sessionRepo.saveAndFlush( dao ) ;
                
                eventBus.publishEvent( SESSION_EXTENDED, new SessionDTO( savedSessionDao ) ) ;

                if( req.getPauseId() > 0 ) {
                    SessionPause pauseDao = sessionPauseRepo.findById( req.getPauseId() ).get() ;
                    pauseDao.setEndTime( req.getEndTime() ) ;
                
                    SessionPause savedSPDao = sessionPauseRepo.saveAndFlush( pauseDao ) ;
                    
                    eventBus.publishEvent( PAUSE_EXTENDED, new SessionPauseDTO( savedSPDao )  );
                }
                
                if( req.getProblemAttemptId() > 0 ) {
                    ProblemAttempt paDao = paRepo.findById( req.getProblemAttemptId() ).get() ;
                    paDao.setEndTime( req.getEndTime() ) ;
                    paDao.setEffectiveDuration( req.getProblemAttemptEffectiveDuration() ) ;
                
                    ProblemAttempt savedPADao = paRepo.saveAndFlush( paDao ) ;
                    
                    eventBus.publishEvent( PROBLEM_ATTEMPT_EXTENDED, new ProblemAttemptDTO( savedPADao ) ) ;
                }
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
