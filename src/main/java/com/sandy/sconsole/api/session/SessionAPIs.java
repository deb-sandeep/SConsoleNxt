package com.sandy.sconsole.api.session;

import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.master.*;
import com.sandy.sconsole.dao.session.ProblemAttempt;
import com.sandy.sconsole.dao.session.Session;
import com.sandy.sconsole.dao.session.SessionPause;
import com.sandy.sconsole.dao.session.dto.ProblemAttemptDTO;
import com.sandy.sconsole.dao.session.dto.SessionDTO;
import com.sandy.sconsole.dao.session.dto.SessionPauseDTO;
import com.sandy.sconsole.dao.master.repo.*;
import com.sandy.sconsole.dao.session.repo.ProblemAttemptRep;
import com.sandy.sconsole.dao.session.repo.SessionPauseRepo;
import com.sandy.sconsole.dao.session.repo.SessionRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.sandy.sconsole.core.api.AR.*;

@Slf4j
@RestController
@RequestMapping( "/Session" )
public class SessionAPIs {
    
    @Autowired private SessionTypeRepo   stRepo ;
    @Autowired private SessionRepo       sessionRepo ;
    @Autowired private SessionPauseRepo  sessionPauseRepo ;
    @Autowired private TopicRepo         topicRepo ;
    @Autowired private TopicProblemRepo  tpRepo ;
    @Autowired private ProblemRepo       problemRepo ;
    @Autowired private ProblemAttemptRep paRepo ;
    @Autowired private ProblemAttemptRep problemAttemptRep;
    
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
            
            Session savedDao = sessionRepo.save( dao ) ;
            
            return success( savedDao.getId() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @PostMapping( "/StartProblemAttempt" )
    public ResponseEntity<AR<Integer>> startProblemAttempt( @RequestBody ProblemAttemptDTO req ) {
        try {
            ProblemAttempt pa = new ProblemAttempt() ;
            pa.setSession( sessionRepo.findById( req.getSessionId() ).get() ) ;
            pa.setProblem( problemRepo.findById( req.getProblemId() ).get() ) ;
            pa.setStartTime( req.getStartTime() ) ;
            pa.setEndTime( req.getEndTime() ) ;
            pa.setEffectiveDuration( req.getEffectiveDuration() ) ;
            pa.setPrevState( req.getPrevState() ) ;
            pa.setTargetState( req.getTargetState() ) ;
            
            return success( paRepo.save( pa ).getId() ) ;
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
            paRepo.save( pa ) ;
            
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
            
            SessionPause savedDao = sessionPauseRepo.save( dao ) ;
            
            return success( savedDao.getId() ) ;
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
                sessionRepo.save( dao ) ;

                if( req.getPauseId() > 0 ) {
                    SessionPause pauseDao = sessionPauseRepo.findById( req.getPauseId() ).get() ;
                    pauseDao.setEndTime( req.getEndTime() ) ;
                    sessionPauseRepo.save( pauseDao ) ;
                }
                
                if( req.getProblemAttemptId() > 0 ) {
                    ProblemAttempt paDao = problemAttemptRep.findById( req.getProblemAttemptId() ).get() ;
                    paDao.setEndTime( req.getEndTime() ) ;
                    paDao.setEffectiveDuration( req.getProblemAttemptEffectiveDuration() ) ;
                    problemAttemptRep.save( paDao ) ;
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
                return success( tpRepo.findActiveProblemsByTopicId( session.getTopic().getId() )) ;
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
                return success( tpRepo.findPigeonedProblemsByTopicId( session.getTopic().getId() )) ;
            }
            return functionalError( "No active session" ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
}
