package com.sandy.sconsole.ui.util;

import com.google.common.collect.ArrayListMultimap;
import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.clock.ClockTickListener;
import com.sandy.sconsole.core.clock.SConsoleClock;
import com.sandy.sconsole.dao.master.TopicTrackAssignment;
import com.sandy.sconsole.dao.master.repo.TopicTrackAssignmentRepo;
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
public class ActiveTopicStatisticsManager implements ClockTickListener {
    
    private static final int[] SUBSCRIBED_EVENTS = {} ;
    
    @Autowired private SConsoleClock clock ;
    @Autowired private EventBus eventBus ;
    
    @Autowired private TopicTrackAssignmentRepo ttaRepo ;
    
    private final ArrayListMultimap<String, ActiveTopicStatistics> syllabusActiveTopics  = ArrayListMultimap.create() ;
    private final Map<Integer, ActiveTopicStatistics>              topicActiveStatistics = new HashMap<>() ;
    
    @PostConstruct
    public void init() throws ParseException {
        log.debug( "Initializing ActiveTopicStatisticsManager..." ) ;
        
        clock.addTickListener( this, TimeUnit.DAYS ) ;
        
        // TODO: This is for development purposes only. Remove before
        // production release.
        Date date = DateUtils.parseDate( "2025-04-10", "YYYY-MM-dd" ) ;
        refreshState( date ) ;
    }
    
    @Override
    public void dayTicked( Calendar date ) {
        refreshState( date.getTime() ) ;
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
