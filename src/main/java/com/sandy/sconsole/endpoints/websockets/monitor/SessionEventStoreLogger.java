package com.sandy.sconsole.endpoints.websockets.monitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.bus.EventTargetMarker;
import com.sandy.sconsole.dao.audit.SessionEvent;
import com.sandy.sconsole.dao.audit.repo.SessionEventRepo;
import com.sandy.sconsole.dao.master.Problem;
import com.sandy.sconsole.dao.master.repo.ProblemRepo;
import com.sandy.sconsole.dao.session.Session;
import com.sandy.sconsole.dao.session.dto.ProblemAttemptDTO;
import com.sandy.sconsole.dao.session.dto.SessionDTO;
import com.sandy.sconsole.dao.session.dto.SessionPauseDTO;
import com.sandy.sconsole.dao.session.repo.SessionRepo;
import com.sandy.sconsole.endpoints.rest.live.SessionExtensionDTO;
import com.sandy.sconsole.endpoints.websockets.monitor.payload.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.sandy.sconsole.EventCatalog.*;
import static com.sandy.sconsole.core.bus.EventUtils.getEventName;

@Slf4j
@Component
public class SessionEventStoreLogger implements EventSubscriber {
    
    @Autowired EventBus eventBus ;
    @Autowired SessionRepo sessionRepo ;
    @Autowired ProblemRepo problemRepo ;
    @Autowired SessionEventRepo eventRepo ;
    @Autowired AppMonitorWSController appMonWebSocket ;
    
    private final ObjectMapper jsonMapper = new ObjectMapper() ;
    
    @PostConstruct
    public void init() {
        eventBus.addAsyncSubscriber( this, SESSION_STARTED ) ;
        eventBus.addAsyncSubscriber( this, SESSION_ENDED ) ;
        eventBus.addAsyncSubscriber( this, PROBLEM_ATTEMPT_STARTED ) ;
        eventBus.addAsyncSubscriber( this, PROBLEM_ATTEMPT_ENDED ) ;
        eventBus.addAsyncSubscriber( this, PAUSE_STARTED ) ;
        eventBus.addAsyncSubscriber( this, PAUSE_ENDED ) ;
        eventBus.addAsyncSubscriber( this, SESSION_EXTENDED ) ;
    }
    
    @Override
    public synchronized void handleEvent( Event event ) {
        switch( event.getEventId() ) {
            case SESSION_STARTED -> saveSessionStarted( ( SessionDTO )event.getValue() ) ;
            case SESSION_ENDED -> saveSessionEnded( ( Integer )event.getValue() ) ;
            case PROBLEM_ATTEMPT_STARTED -> saveProblemAttemptStarted( ( ProblemAttemptDTO )event.getValue() ) ;
            case PROBLEM_ATTEMPT_ENDED -> saveProblemAttemptEnded( ( ProblemAttemptDTO )event.getValue() ) ;
            case PAUSE_STARTED -> savePauseStarted( ( SessionPauseDTO )event.getValue() ) ;
            case PAUSE_ENDED -> savePauseEnded( ( SessionPauseDTO )event.getValue() ) ;
            case SESSION_EXTENDED -> notifySessionExtension( ( SessionExtensionDTO )event.getValue() ) ;
        }
    }
    
    @EventTargetMarker( SESSION_STARTED )
    private void saveSessionStarted( SessionDTO session ) {
        
        SessionStart event = new SessionStart() ;
        event.setSessionId( session.getId() ) ;
        event.setStartTime( session.getStartTime() ) ;
        event.setSessionType( session.getSessionType() ) ;
        event.setSyllabusName( session.getSyllabusName() ) ;
        event.setTopicName( session.getTopicName() ) ;
        
        persistEvent( getEventName( SESSION_STARTED ), event ) ;
    }
    
    @EventTargetMarker( SESSION_ENDED )
    private void saveSessionEnded( Integer sessionId ) {
        
        Session session = sessionRepo.findById( sessionId ).get() ;
        
        SessionEnd event = new SessionEnd() ;
        event.setSessionId( session.getId() ) ;
        event.setStartTime( session.getStartTime() ) ;
        event.setEndTime( session.getEndTime() ) ;
        event.setEffectiveDuration( session.getEffectiveDuration() ) ;
        event.setSessionType( session.getSessionType() ) ;
        event.setSyllabusName( session.getSyllabusName() ) ;
        event.setTopicName( session.getTopic().getTopicName() ) ;
        
        persistEvent( getEventName( SESSION_ENDED ), event ) ;
    }
    
    @EventTargetMarker( PROBLEM_ATTEMPT_STARTED )
    private void saveProblemAttemptStarted( ProblemAttemptDTO attempt ) {
        
        Session session = sessionRepo.findById( attempt.getSessionId() ).get() ;
        Problem problem = problemRepo.findById( attempt.getProblemId() ).get() ;
        
        ProblemAttemptStart event = new ProblemAttemptStart() ;
        event.setSessionId( attempt.getSessionId() ) ;
        event.setProblemAttemptId( attempt.getId() ) ;
        event.setStartTime( attempt.getStartTime() ) ;
        event.setSyllabusName( session.getSyllabusName() ) ;
        event.setTopicName( session.getTopic().getTopicName() ) ;
        event.setBookName( problem.getChapter().getBook().getBookShortName() ) ;
        event.setChapterNum( problem.getChapter().getId().getChapterNum() ) ;
        event.setChapterName( problem.getChapter().getChapterName() ) ;
        event.setProblemKey( problem.getProblemKey() ) ;
        event.setCurrentState( attempt.getPrevState() ) ;
        
        persistEvent( getEventName( PROBLEM_ATTEMPT_STARTED ), event ) ;
    }
    
    @EventTargetMarker( PROBLEM_ATTEMPT_ENDED )
    private void saveProblemAttemptEnded( ProblemAttemptDTO attempt ) {
        
        Session session = sessionRepo.findById( attempt.getSessionId() ).get() ;
        Problem problem = problemRepo.findById( attempt.getProblemId() ).get() ;
        
        ProblemAttemptEnd event = new ProblemAttemptEnd() ;
        event.setSessionId( attempt.getSessionId() ) ;
        event.setProblemAttemptId( attempt.getId() ) ;
        event.setStartTime( attempt.getStartTime() ) ;
        event.setEndTime( attempt.getEndTime() ) ;
        event.setSyllabusName( session.getSyllabusName() ) ;
        event.setTopicName( session.getTopic().getTopicName() ) ;
        event.setBookName( problem.getChapter().getBook().getBookShortName() ) ;
        event.setChapterNum( problem.getChapter().getId().getChapterNum() ) ;
        event.setChapterName( problem.getChapter().getChapterName() ) ;
        event.setProblemKey( problem.getProblemKey() ) ;
        event.setTargetState( attempt.getTargetState() ) ;
        event.setEffectiveDuration( attempt.getEffectiveDuration() ) ;
        
        persistEvent( getEventName( PROBLEM_ATTEMPT_ENDED ), event ) ;
    }
    
    @EventTargetMarker( PAUSE_STARTED )
    private void savePauseStarted( SessionPauseDTO pause ) {
        
        PauseStart pauseStart = new PauseStart() ;
        pauseStart.setPauseId( pause.getId() ) ;
        pauseStart.setSessionId( pause.getSessionId() ) ;
        pauseStart.setStartTime( pause.getStartTime() ) ;
        
        persistEvent( getEventName( PAUSE_STARTED ), pauseStart ) ;
    }
    
    @EventTargetMarker( PAUSE_ENDED )
    private void savePauseEnded( SessionPauseDTO pause ) {

        PauseEnd pauseEnd = new PauseEnd() ;
        pauseEnd.setPauseId( pause.getId() ) ;
        pauseEnd.setSessionId( pause.getSessionId() ) ;
        pauseEnd.setStartTime( pause.getStartTime() ) ;
        pauseEnd.setEndTime( pause.getEndTime() ) ;
        pauseEnd.setDuration( pause.getDuration() ) ;
        
        persistEvent( getEventName( PAUSE_ENDED ), pauseEnd ) ;
    }
    
    @EventTargetMarker( SESSION_EXTENDED )
    private void notifySessionExtension( SessionExtensionDTO sessionEx ) {
        
        Map<String, Integer> payload = new HashMap<>() ;
        payload.put( "sessionId", sessionEx.getSessionDTO().getId() ) ;
        payload.put( "sessionEffectiveDuration", sessionEx.getSessionDTO().getEffectiveDuration() ) ;
        
        if( sessionEx.getProblemAttemptDTO() != null ) {
            payload.put( "problemAttemptId", sessionEx.getProblemAttemptDTO().getId() ) ;
            payload.put( "problemAttemptEffectiveDuration", sessionEx.getProblemAttemptDTO().getEffectiveDuration() ) ;
        }
        if( sessionEx.getPauseDTO() != null ) {
            payload.put( "pauseId", sessionEx.getPauseDTO().getId() ) ;
            payload.put( "pauseDuration", sessionEx.getPauseDTO().getDuration() ) ;
        }
        
        appMonWebSocket.sendMessage( AppMonitorWSController.ResponseType.SESSION_EVENT,
                                     new SessionEventDTO( getEventName( SESSION_EXTENDED ), new Date(), payload ) ) ;
    }
    
    private void persistEvent( String eventId, Object payload ) {
        try {
            String jsonPayload = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString( payload ) ;
            
            SessionEvent event = new SessionEvent() ;
            event.setEventId( eventId ) ;
            event.setTime( new Date() ) ;
            event.setPayload( jsonPayload ) ;
            eventRepo.saveAndFlush( event ) ;
            
            appMonWebSocket.sendMessage( AppMonitorWSController.ResponseType.SESSION_EVENT,
                                         new SessionEventDTO( eventId, event.getTime(), payload ) ) ;
        }
        catch( Exception e ) {
            log.error( "Error while persisting event payload", e ) ;
        }
    }
}
