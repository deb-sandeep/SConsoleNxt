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
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * This keeps an in-memory cache of statistics regarding active topics of the
 * current day and publishes events in case any statistics changes. For each
 * of the active topics it manages an instance of ActiveTopicStatistics and
 * updates their state accordingly.
 *
 * The state of this cache is updated at three levels of granularity:
 *
 * -> Full refresh : Active topics are updated and the entire state is computed
 *   fresh. This happens under the following situations:
 *   - Post construct when this object is created for the first time
 *   - At day change
 *   - If any of the tracks are updated
 *
 * -> Topic burn refresh : Burn statistics of a particular active topic is updated.
 *   This is the most common scenario which happens whenever a problem attempt
 *   has ended. The problem attempt is analyzed to determine if it can result in
 *   a burn statistics change and if so, the topic state is updated.
 *
 * Events Consumed:
 * ----------------
 * 1. Day tick -> Full refresh
 * 2. Track assignment changed - Full refresh
 * 2. Problem attempt completed -> Topic burn refresh
 *
 */
@Slf4j
@Component
@DependsOn( {"clock", "eventBus" } )
public class ActiveTopicStatisticsManager implements ClockTickListener, EventSubscriber {
    
    private static final int[] SUBSCRIBED_EVENTS = {
            EventCatalog.PROBLEM_ATTEMPT_ENDED,
            EventCatalog.TRACK_UPDATED
    } ;
    
    // TODO: Remove this once the system becomes live. A live system should use current date
    private Date REF_DATE = null ;
    
    @Autowired private SConsoleClock clock ;
    @Autowired private EventBus eventBus ;
    
    @Autowired private TopicTrackAssignmentRepo ttaRepo ;
    @Autowired private SessionRepo sessionRepo ;
    
    private final Map<Integer, ActiveTopicStatistics> topicActiveStatistics = new HashMap<>() ; // Key -> Topic Id
    private final ArrayListMultimap<String, ActiveTopicStatistics> syllabusActiveTopics  = ArrayListMultimap.create() ; // Key -> Syllabus Name
    
    @PostConstruct
    public void init() throws ParseException {
        log.debug( "Initializing ActiveTopicStatisticsManager..." ) ;
        
        clock.addTickListener( this, TimeUnit.DAYS ) ;
        eventBus.addSubscriberForEventTypes( this, true, SUBSCRIBED_EVENTS ) ;
        
        // TODO: This is for development purposes only. Remove before production release.
        REF_DATE = DateUtils.parseDate( "2025-04-10", "YYYY-MM-dd" ) ;
        refreshState( REF_DATE ) ;
    }
    
    @Override
    public void dayTicked( Calendar date ) {
        refreshState( date.getTime() ) ;
    }
    
    @Override
    public void handleEvent( Event event ) {
        int eventType = event.getEventType() ;
        switch ( eventType ) {
            case EventCatalog.TRACK_UPDATED -> refreshState( REF_DATE ) ;
            case EventCatalog.PROBLEM_ATTEMPT_ENDED -> _handleProblemAttemptEnded( (ProblemAttemptDTO)event.getValue() ) ;
        }
    }
    
    private void refreshState( Date date ) {
        
        log.debug( "  Refreshing state of ActiveTopicStatisticsManager..." ) ;
        log.debug( "    Using date {}", date ) ;
        
        clearState() ;
        
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
    
    public List<ActiveTopicStatistics> getTopicStatistics( String syllabusName ) {
        return syllabusActiveTopics.get( syllabusName ) ;
    }
}
