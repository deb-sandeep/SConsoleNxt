package com.sandy.sconsole.state.manager;

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
 */
@Slf4j
@Component
public class TodayStudyStatistics
    implements EventSubscriber, ClockTickListener {
    
    @Autowired private EventBus eventBus ;
    @Autowired private SConsoleClock clock ;
    @Autowired private SessionRepo sessionRepo ;
    @Autowired private SessionPauseRepo pauseRepo;
    @Autowired private ActiveTopicStatisticsManager activeTopicStatsMgr;
    
    // Functional state. These need to be reset in initializeFunctionalState method
    private final Map<Integer, SessionDTO>      allSessions = new LinkedHashMap<>() ; // Key = Session ID
    private final Map<Integer, SessionPauseDTO> allPauses   = new LinkedHashMap<>() ; // Key = Pause ID
    
    // Pauses per session. Key = Session ID
    private final ArrayListMultimap<Integer, SessionPauseDTO> sessionPauses = ArrayListMultimap.create();
    
    // Sessions per syllabus. Key = Syllabus Name
    private final ArrayListMultimap<String, SessionDTO> syllabusSessions = ArrayListMultimap.create();
    
    // Stores "effective" times for each syllabus. Key = Syllabus Name
    private final Map<String, Integer> syllabusTimes = new HashMap<>() ;
    
    private Day today = new Day() ;
    
    @Getter
    private int totalEffectiveTimeInSec = 0 ;
    
    // If a session is ongoing, this will store a reference to that session
    // else, null.
    @Getter
    SessionDTO currentSession ;
    
    @PostConstruct
    public void init() {
        clock.addTickListener( this, TimeUnit.DAYS ) ;
        subscribeToEvents() ;
        initializeState() ;
    }
    
    private void subscribeToEvents() {
        // Consume the events synchronously. Why? Because the session screen
        // will ask for the live session before activation and handling a
        // session start event needs to be deterministically done before that.
        // NOTE: SESSION_STARTED and SESSION_ENDED events are not registered
        //       as they are called on this bean directly from the API
        eventBus.addSyncSubscriber( this,
                SESSION_EXTENDED,
                PAUSE_STARTED,
                PAUSE_EXTENDED ) ;
        
        eventBus.addAsyncSubscriber( this, HISTORIC_SESSION_UPDATED ) ;
    }
    
    @Override
    public void handleEvent( Event event ) {
        
        final int eventType = event.getEventId() ;
        switch( eventType ) {
            case HISTORIC_SESSION_UPDATED:
                initializeState() ;
                break ;
            
            case SESSION_EXTENDED:
                SessionDTO session = ( SessionDTO )event.getValue() ;
                currentSession = updateCachedSession( session, true ) ;
                break ;
            
            case PAUSE_STARTED:
            case PAUSE_EXTENDED:
                updateCachedPause( (SessionPauseDTO)event.getValue(), true ) ;
                break ;
        }
    }
    
    @Override
    public void dayTicked( Calendar calendar ) {
        initializeState() ;
    }
    
    private void initializeState() {
        
        today = new Day() ;
        allSessions.clear() ;
        allPauses.clear() ;
        sessionPauses.clear() ;
        syllabusSessions.clear() ;
        syllabusTimes.clear() ;
        
        totalEffectiveTimeInSec = 0 ;
        
        // Load the existing sessions and pauses for today
        sessionRepo.getTodaySessions()
                   .forEach( s -> updateCachedSession( new SessionDTO( s ), false ) ) ;
        
        pauseRepo.getTodayPauses()
                 .forEach( p -> updateCachedPause( new SessionPauseDTO( p ), false ) ) ;
        
        eventBus.publishEvent( TODAY_STUDY_STATS_UPDATED ) ;
    }
    
    public Collection<SessionDTO> getAllSessions() { return allSessions.values() ; }
    
    public Collection<SessionPauseDTO> getAllPauses() { return allPauses.values() ; }
    
    public int getNumProblemsSolvedToday( int topicId ) {
        return activeTopicStatsMgr.getTopicStatistics( topicId ).getNumProblemsSolvedToday() ;
    }
    
    public int getSyllabusTime( String syllabusName ) {
        if( syllabusTimes.containsKey( syllabusName ) ) {
            return syllabusTimes.get( syllabusName ) ;
        }
        return 0 ;
    }
    
    public void sessionStarted( SessionDTO session ) {
        currentSession = updateCachedSession( session, true ) ;
    }
    
    public void sessionEnded() {
        currentSession = null ;
    }
    
    public SessionDTO updateCachedSession( @NonNull SessionDTO _session, boolean emitEvents ) {
        
        SessionDTO session = allSessions.computeIfAbsent( _session.getId(), k -> {
            SessionDTO newSession = new SessionDTO( _session ) ;
            syllabusSessions.put( newSession.getSyllabusName(), newSession ) ;
            return newSession ;
        } ) ;
        
        session.absorb( _session ) ;
        
        if( today.startsAfter( session.getStartTime() ) ) {
            session.setStartTime( today.getStartTime() ) ;
        }
        else if( today.endsBefore( session.getEndTime() ) ) {
            session.setEndTime( today.getEndTime() ) ;
        }
        
        computeTotalEffectiveTimeForToday( emitEvents ) ;
        return session ;
    }
    
    public void updateCachedPause( @NonNull SessionPauseDTO _pause, boolean emitEvents ) {
        
        SessionPauseDTO pause = allPauses.computeIfAbsent( _pause.getId(), k -> {
            SessionPauseDTO newPause = new SessionPauseDTO( _pause ) ;
            sessionPauses.put( newPause.getSessionId(), newPause ) ;
            return newPause ;
        } ) ;
        
        pause.absorb( _pause ) ;
        
        if( today.startsAfter( pause.getStartTime() ) ) {
            pause.setStartTime( today.getStartTime() ) ;
        }
        else if( today.endsBefore( pause.getEndTime() ) ) {
            pause.setEndTime( today.getEndTime() ) ;
        }
        
        computeTotalEffectiveTimeForToday( emitEvents ) ;
    }
    
    private void computeTotalEffectiveTimeForToday( boolean emitEvents ) {
        
        int totalSessionTime = allSessions.values().stream().mapToInt( SessionDTO::getDuration ).sum() ;
        int totalPauseTime = allPauses.values().stream().mapToInt( SessionPauseDTO::getDuration ).sum() ;
        
        this.totalEffectiveTimeInSec = totalSessionTime - totalPauseTime ;
        
        syllabusSessions.keySet().forEach( syllabusName -> {
            
            int totalSyllabusTime = 0 ;
            for( SessionDTO session : syllabusSessions.get( syllabusName ) ) {
                
                totalSyllabusTime += session.getDuration() ;
                for( SessionPauseDTO pause : sessionPauses.get( session.getId() ) ) {
                    totalSyllabusTime -= pause.getDuration() ;
                }
            }
            syllabusTimes.put( syllabusName, totalSyllabusTime ) ;
        } ) ;
        
        if( emitEvents ) {
            eventBus.publishEvent( TODAY_EFFORT_UPDATED ) ;
        }
    }
}
