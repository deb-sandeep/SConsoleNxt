package com.sandy.sconsole.state;

import com.google.common.collect.ArrayListMultimap;
import com.sandy.sconsole.AppConstants;
import com.sandy.sconsole.EventCatalog;
import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.clock.ClockTickListener;
import com.sandy.sconsole.core.clock.SConsoleClock;
import com.sandy.sconsole.dao.master.TopicTrackAssignment;
import com.sandy.sconsole.dao.master.repo.TopicTrackAssignmentRepo;
import com.sandy.sconsole.dao.session.Session;
import com.sandy.sconsole.dao.session.dto.ProblemAttemptDTO;
import com.sandy.sconsole.dao.session.repo.SessionRepo;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Consumes:
 * 1. Day tick
 * 2. Problem attempt completed (event bus)
 * 3. Topic track assignment changed
 *
 *
 */
@Slf4j
@Component
public class ActiveTopicStatisticsManager implements ClockTickListener, EventSubscriber {
    
    private static final int[] SUBSCRIBED_EVENTS = {
            EventCatalog.PROBLEM_ATTEMPT_ENDED
    } ;
    
    @Autowired private SConsoleClock clock ;
    @Autowired private EventBus eventBus ;
    
    @Autowired private TopicTrackAssignmentRepo ttaRepo ;
    @Autowired private SessionRepo sessionRepo ;
    
    private final ArrayListMultimap<String, ActiveTopicStatistics> syllabusActiveTopics  = ArrayListMultimap.create() ;
    private final Map<Integer, ActiveTopicStatistics>              topicActiveStatistics = new HashMap<>() ;
    
    @PostConstruct
    public void init() throws ParseException {
        log.debug( "Initializing ActiveTopicStatisticsManager..." ) ;
        
        clock.addTickListener( this, TimeUnit.DAYS ) ;
        eventBus.addSubscriberForEventTypes( this, true, SUBSCRIBED_EVENTS ) ;
        
        // TODO: This is for development purposes only. Remove before production release.
        Date date = DateUtils.parseDate( "2025-04-10", "YYYY-MM-dd" ) ;
        refreshState( date ) ;
    }
    
    @Override
    public void dayTicked( Calendar date ) {
        refreshState( date.getTime() ) ;
    }
    
    @Override
    public void handleEvent( Event event ) {
        if( event.getEventType() == EventCatalog.PROBLEM_ATTEMPT_ENDED ) {
            _handleProblemAttemptEnded( (ProblemAttemptDTO)event.getValue() ) ;
        }
    }
    
    private void _handleProblemAttemptEnded( ProblemAttemptDTO pa ) {
        String targetState = pa.getTargetState() ;
        if( targetState.equals( AppConstants.PROBLEM_STATE_CORRECT ) ||
            targetState.equals( AppConstants.PROBLEM_STATE_INCORRECT ) ||
            targetState.equals( AppConstants.PROBLEM_STATE_PURGE ) ||
            targetState.equals( AppConstants.PROBLEM_STATE_PIGEON_KILL ) ||
            targetState.equals( AppConstants.PROBLEM_STATE_REASSIGN ) ) {
            
            Session session = sessionRepo.findById( pa.getSessionId() ).get() ;
            ActiveTopicStatistics ats = topicActiveStatistics.get( session.getTopic().getId() ) ;
            
            if( ats != null ) {
                ats.refreshState() ;
                eventBus.publishEvent( EventCatalog.ATS_REFRESHED, ats.getTopic().getTopicId() ) ;
            }
        }
    }

    private void refreshState( Date date ) {
        
        clearState() ;

        log.debug( "  Refreshing state of ActiveTopicStatisticsManager..." ) ;
        log.debug( "    Using date {}", date ) ;
        
        List<TopicTrackAssignment> activeAssignments = ttaRepo.findActiveAssignments( date ) ;
        log.debug( "    Found {} active assignments", activeAssignments.size() ) ;
        
        activeAssignments.forEach( assignment -> {
            ActiveTopicStatistics ats = SConsole.getBean( ActiveTopicStatistics.class ) ;
            ats.setTopicTrackAssignment( assignment ) ;
            ats.init() ;
            
            syllabusActiveTopics.put( ats.getTopic().getSyllabusName(), ats ) ;
            topicActiveStatistics.put( ats.getTopic().getTopicId(), ats ) ;
        } ) ;
        
        eventBus.publishEvent( EventCatalog.ATS_MANAGER_REFRESHED ) ;
    }
    
    private void clearState() {
        topicActiveStatistics.values().forEach( ActiveTopicStatistics::destroy ) ;
        syllabusActiveTopics.clear() ;
        topicActiveStatistics.clear() ;
    }
    
    public List<ActiveTopicStatistics> getTopicStatstics( String syllabusName ) {
        return syllabusActiveTopics.get( syllabusName ) ;
    }
    
}
