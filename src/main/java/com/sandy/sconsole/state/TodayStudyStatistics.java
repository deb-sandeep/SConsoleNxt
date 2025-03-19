package com.sandy.sconsole.state;

import com.google.common.collect.ArrayListMultimap;
import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.clock.ClockTickListener;
import com.sandy.sconsole.core.clock.SConsoleClock;
import com.sandy.sconsole.core.util.Day;
import com.sandy.sconsole.dao.session.dto.SessionDTO;
import com.sandy.sconsole.dao.session.dto.SessionPauseDTO;
import com.sandy.sconsole.dao.session.repo.SessionPauseRepo;
import com.sandy.sconsole.dao.session.repo.SessionRepo;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.sandy.sconsole.EventCatalog.*;

/**
 * This is a singleton state object which tracks the statistics regarding the
 * study done today. It maintains a cache of sessions, pauses and total
 * effective time qualified by syllabus.
 *
 * This class emits events whenever the internal state changes. Following
 * events are emitted:
 *
 * TODAY_STUDY_STATS_UPDATED : All aspects of today study stats have been updated.
 * This can happen when the system boots up, a day changes or historic sessions
 * have been updated
 *
 * TODAY_STUDY_TIME_UPDATED : Today study time has been updated. This can happen
 * if session or pause has been started or extended.
 *
 */
@Slf4j
@Component
public class TodayStudyStatistics
    implements EventSubscriber, ClockTickListener {
    
    private static final int[] SUBSCRIBED_EVENTS = {
            SESSION_STARTED,
            PAUSE_STARTED,
            SESSION_EXTENDED,
            PAUSE_EXTENDED,
            HISTORIC_SESSION_UPDATED
    } ;
    
    @Autowired private EventBus eventBus ;
    @Autowired private SConsoleClock clock ;
    @Autowired private SessionRepo sessionRepo ;
    @Autowired private SessionPauseRepo pauseRepo;
    
    // Functional state. These need to be reset in initializeFunctionalState method
    private final Map<Integer, SessionDTO> sessions = new LinkedHashMap<>() ; // Key = Session ID
    private final Map<Integer, SessionPauseDTO> pauses = new LinkedHashMap<>() ; // Key = Pause ID
    
    // Pauses per session. Key = Session ID
    private final ArrayListMultimap<Integer, SessionPauseDTO> sessionPauses = ArrayListMultimap.create();
    
    // Sessions per syllabus. Key = Syllabus Name
    private final ArrayListMultimap<String, SessionDTO> syllabusSessions = ArrayListMultimap.create();
    
    private Day today = new Day() ;
    @Getter private int totalTimeInSec = 0 ;
    
    // Stores effective times for each syllabus. Key = Syllabus Name
    private final Map<String, Integer> syllabusTimes = new HashMap<>() ;
    
    @PostConstruct
    public void init() {
        clock.addTickListener( this, TimeUnit.DAYS ) ;
        eventBus.addSubscriberForEventTypes( this, true, SUBSCRIBED_EVENTS) ;
        initializeState() ;
    }
    
    public Collection<SessionDTO> getSessions() { return sessions.values() ; }
    
    public Collection<SessionPauseDTO> getPauses() { return pauses.values() ; }
    
    public int getSyllabusTime( String syllabusName ) {
        if( syllabusTimes.containsKey( syllabusName ) ) {
            return syllabusTimes.get( syllabusName ) ;
        }
        return 0 ;
    }
    
    @Override
    public void dayTicked( Calendar calendar ) {
        initializeState() ;
    }
    
    @Override
    public void handleEvent( Event event ) {
     
        final int eventType = event.getEventType() ;
        switch( eventType ) {
            case HISTORIC_SESSION_UPDATED:
                initializeState() ;
                break ;
                
            case SESSION_STARTED:
            case SESSION_EXTENDED:
                updateCachedSession( (SessionDTO)event.getValue(), true ) ;
                break ;
                
            case PAUSE_STARTED:
            case PAUSE_EXTENDED:
                updateCachedPause( (SessionPauseDTO)event.getValue(), true ) ;
                break ;
        }
    }
    
    private void initializeState() {
        
        today = new Day() ;
        sessions.clear() ;
        pauses.clear() ;
        sessionPauses.clear() ;
        syllabusSessions.clear() ;
        syllabusTimes.clear() ;
        
        totalTimeInSec = 0 ;
        
        // Load the existing sessions and pauses for today
        sessionRepo.getTodaySessions().forEach( s -> updateCachedSession( new SessionDTO( s ), false ) ) ;
        pauseRepo.getTodayPauses().forEach( p -> updateCachedPause( new SessionPauseDTO( p ), false ) ) ;
        
        eventBus.publishEvent( TODAY_STUDY_STATS_UPDATED ) ;
    }
    
    private void updateCachedSession( @NonNull SessionDTO _session, boolean emitEvents ) {
        
        SessionDTO session = sessions.computeIfAbsent( _session.getId(), k -> {
            SessionDTO newSession = new SessionDTO( _session ) ;
            syllabusSessions.put( newSession.getSyllabusName(), newSession ) ;
            return newSession ;
        } ) ;
        session.absorb( _session ) ;
        
        if( today.after( session.getStartTime() ) ) {
            session.setStartTime( today.getStartTime() ) ;
        }
        else if( today.before( session.getEndTime() ) ) {
            session.setEndTime( today.getEndTime() ) ;
        }
        computeTotalEffectiveTimeForToday( emitEvents ) ;
    }
    
    private void updateCachedPause( @NonNull SessionPauseDTO _pause, boolean emitEvents ) {
        
        SessionPauseDTO pause = pauses.computeIfAbsent( _pause.getId(), k -> {
            SessionPauseDTO newPause = new SessionPauseDTO( _pause ) ;
            sessionPauses.put( newPause.getSessionId(), newPause ) ;
            return newPause ;
        } ) ;
        pause.absorb( _pause ) ;
        
        if( today.after( pause.getStartTime() ) ) {
            pause.setStartTime( today.getStartTime() ) ;
        }
        else if( today.before( pause.getEndTime() ) ) {
            pause.setEndTime( today.getEndTime() ) ;
        }
        computeTotalEffectiveTimeForToday( emitEvents ) ;
    }
    
    private void computeTotalEffectiveTimeForToday( boolean emitEvents ) {
        
        int totalSessionTime = sessions.values().stream().mapToInt( SessionDTO::getDuration ).sum() ;
        int totalPauseTime = pauses.values().stream().mapToInt( SessionPauseDTO::getDuration ).sum() ;
        this.totalTimeInSec = totalSessionTime - totalPauseTime ;
        
        syllabusSessions.keySet().forEach( syllabusName -> {
            List<SessionDTO> sessions = syllabusSessions.get( syllabusName ) ;
            int totalSyllabusTime = 0 ;
            for( SessionDTO session : sessions ) {
                totalSyllabusTime += session.getDuration() ;
                for( SessionPauseDTO pause : pauses.values() ) {
                    totalSyllabusTime -= pause.getDuration() ;
                }
            }
            syllabusTimes.put( syllabusName, totalSyllabusTime ) ;
        } ) ;
        
        if( emitEvents ) {
            eventBus.publishEvent( TODAY_STUDY_TIME_UPDATED ) ;
        }
    }
}
