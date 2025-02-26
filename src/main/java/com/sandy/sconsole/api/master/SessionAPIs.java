package com.sandy.sconsole.api.master;

import com.sandy.sconsole.api.master.vo.reqres.NewSessionReq;
import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.master.Session;
import com.sandy.sconsole.dao.master.SessionType;
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
    
    @Autowired private SessionTypeRepo stRepo ;
    @Autowired private SessionRepo     sessionRepo ;
    @Autowired private TopicRepo       topicRepo ;
    
    @GetMapping( "/Types" )
    public ResponseEntity<AR<List<SessionType>>> getAllSessionTypes() {
        try {
            return AR.success( stRepo.findAll() ) ;
        }
        catch( Exception e ) {
            return AR.systemError( e ) ;
        }
    }
    
    @PostMapping( "/NewSession" )
    public ResponseEntity<AR<Integer>> createNewSession( @RequestBody NewSessionReq req ) {
        try {
            Session session = new Session() ;
            session.setSessionType( req.getSessionType() ) ;
            session.setTopic( topicRepo.findById( req.getTopicId() ).get() ) ;
            session.setSyllabusName( req.getSyllabusName() ) ;
            session.setStartTime( req.getStartTime().toInstant() ) ;
            session.setEndTime( session.getStartTime() ) ;
            
            Session savedSession = sessionRepo.save( session ) ;
            
            return AR.success( savedSession.getId() ) ;
        }
        catch( Exception e ) {
            return AR.systemError( e ) ;
        }
    }
}
