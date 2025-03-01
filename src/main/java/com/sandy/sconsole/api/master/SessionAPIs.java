package com.sandy.sconsole.api.master;

import com.sandy.sconsole.api.master.vo.reqres.ExtendSessionReq;
import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.master.Session;
import com.sandy.sconsole.dao.master.SessionPause;
import com.sandy.sconsole.dao.master.SessionType;
import com.sandy.sconsole.dao.master.dto.SessionDTO;
import com.sandy.sconsole.dao.master.dto.SessionPauseDTO;
import com.sandy.sconsole.dao.master.repo.SessionPauseRepo;
import com.sandy.sconsole.dao.master.repo.SessionRepo;
import com.sandy.sconsole.dao.master.repo.SessionTypeRepo;
import com.sandy.sconsole.dao.master.repo.TopicRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping( "/Master/Session" )
public class SessionAPIs {
    
    @Autowired private SessionTypeRepo  stRepo ;
    @Autowired private SessionRepo      sessionRepo ;
    @Autowired private SessionPauseRepo sessionPauseRepo ;
    @Autowired private TopicRepo        topicRepo ;
    
    @GetMapping( "/Types" )
    public ResponseEntity<AR<List<SessionType>>> getAllSessionTypes() {
        try {
            return AR.success( stRepo.findAll() ) ;
        }
        catch( Exception e ) {
            return AR.systemError( e ) ;
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
            
            return AR.success( savedDao.getId() ) ;
        }
        catch( Exception e ) {
            return AR.systemError( e ) ;
        }
    }
    
    @PostMapping( "/EndSession" )
    public ResponseEntity<AR<String>> endSession( @RequestBody SessionDTO req ) {
        try {
            Session dao = sessionRepo.findById( req.getId() ).get() ;
            dao.setEndTime( req.getEndTime() ) ;
            dao.setEffectiveDuration( req.getEffectiveDuration() ) ;
            sessionRepo.save( dao ) ;
            
            return AR.success() ;
        }
        catch( Exception e ) {
            return AR.systemError( e ) ;
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
            
            return AR.success( savedDao.getId() ) ;
        }
        catch( Exception e ) {
            return AR.systemError( e ) ;
        }
    }
    
    @PostMapping( "/EndPause" )
    public ResponseEntity<AR<String>> endPause( @RequestBody SessionPauseDTO req ) {
        try {
            SessionPause dao = sessionPauseRepo.findById( req.getId() ).get() ;
            dao.setEndTime( req.getEndTime() ) ;
            sessionPauseRepo.save( dao ) ;
            return AR.success() ;
        }
        catch( Exception e ) {
            return AR.systemError( e ) ;
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
                    // TODO: Add extension for problem attempts
                }
                
                return AR.success() ;
            }
            return AR.success( "No active session" ) ;
        }
        catch( Exception e ) {
            return AR.systemError( e ) ;
        }
    }
    
    // TODO: Problem attempt started, ended
}
