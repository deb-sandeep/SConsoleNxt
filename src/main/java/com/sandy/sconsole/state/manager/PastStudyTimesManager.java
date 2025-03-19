package com.sandy.sconsole.state.manager;

import com.sandy.sconsole.AppConstants;
import com.sandy.sconsole.EventCatalog;
import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.clock.ClockTickListener;
import com.sandy.sconsole.core.clock.SConsoleClock;
import com.sandy.sconsole.core.util.DayValue;
import com.sandy.sconsole.state.PastStudyTimes;
import com.sandy.sconsole.state.SyllabusPastStudyTimes;
import com.sandy.sconsole.state.TotalPastStudyTimes;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This keeps an in-memory cache of past study times, both at a syllabus level
 * and cumulative.
 *
 * The state of this cache is updated at three levels of granularity:
 *
 * -> Full refresh : Current and past dates are computed and data loaded from
 *    the database. This happens during:
 *   - Post construct when this object is created for the first time
 *   - At day change
 *   - If any of the tracks are updated
 *   - Historic sessions updated
 *
 * -> Syllabus study time refresh : Whenever a TODAY_STUDY_TIME_UPDATED
 *    event is published.
 *
 * Once the state is updated, a PAST_STUDY_TIME_UPDATED event is published
 * on the event bus.
 *
 * Events Consumed:
 * ----------------
 * 1. Day tick -> Full refresh
 * 2. HISTORIC_SESSION_UPDATED -> Full refresh
 * 3. TODAY_STUDY_TIME_UPDATED -> Only today time updates
 *
 */
@Slf4j
@Component
@DependsOn( {"clock", "eventBus" } )
public class PastStudyTimesManager implements ClockTickListener, EventSubscriber {
    
    private static final int[] SUBSCRIBED_EVENTS = {
            EventCatalog.HISTORIC_SESSION_UPDATED,
            EventCatalog.TODAY_STUDY_TIME_UPDATED,
    } ;
    
    @Autowired private SConsoleClock clock ;
    @Autowired private EventBus eventBus ;
    
    private final PastStudyTimes totalPastStudyTimes = new TotalPastStudyTimes();
    
    private final PastStudyTimes[] allPastStudyTimes = {
       new SyllabusPastStudyTimes( AppConstants.IIT_PHY_SYLLABUS_NAME ),
       new SyllabusPastStudyTimes( AppConstants.IIT_CHEM_SYLLABUS_NAME ),
       new SyllabusPastStudyTimes( AppConstants.IIT_MATHS_SYLLABUS_NAME ),
       new SyllabusPastStudyTimes( AppConstants.REASONING_SYLLABUS_NAME ),
       totalPastStudyTimes
    } ;
    
    private final Map<String, PastStudyTimes> syllabusStudyTimesMap = new HashMap<>() ;
    
    public PastStudyTimesManager() {
        Arrays.stream( allPastStudyTimes ).forEach( pastStudyTimes -> {
            if( pastStudyTimes instanceof SyllabusPastStudyTimes ) {
                syllabusStudyTimesMap.put( (( SyllabusPastStudyTimes )pastStudyTimes).getSyllabusName(), pastStudyTimes ) ;
            }
        }) ;
    }
    
    @PostConstruct
    public void init() {
        clock.addTickListener( this, TimeUnit.DAYS ) ;
        eventBus.addSubscriberForEventTypes( this, true, SUBSCRIBED_EVENTS ) ;
        fullRefresh() ;
    }
    
    @Override
    public void dayTicked( Calendar date ) {
        fullRefresh() ;
    }
    
    @Override
    public void handleEvent( Event event ) {
        int eventType = event.getEventType() ;
        switch ( eventType ) {
            case EventCatalog.HISTORIC_SESSION_UPDATED -> fullRefresh() ;
            case EventCatalog.TODAY_STUDY_TIME_UPDATED -> updateTodayTime() ;
        }
    }
    
    public PastStudyTimes getPastStudyTimes( String syllabusName ) {
        return syllabusStudyTimesMap.get( syllabusName ) ;
    }
    
    public PastStudyTimes getPastStudyTimes() {
        return totalPastStudyTimes ;
    }
    
    private void fullRefresh() {
        Arrays.stream( allPastStudyTimes ).forEach( item -> {
            item.clearState() ;
            item.fullRefresh() ;
        } ) ;
        eventBus.publishEvent( EventCatalog.PAST_STUDY_TIME_UPDATED ) ;
    }
    
    private void updateTodayTime() {
        Arrays.stream( allPastStudyTimes ).forEach( PastStudyTimes::updateTodayTime ) ;
        eventBus.publishEvent( EventCatalog.PAST_STUDY_TIME_UPDATED ) ;
    }
    
    // Returns the max day value across the historic data of all syllabus
    public double getMaxSyllabusTime() {
        return syllabusStudyTimesMap.values()
                .stream()
                .mapToDouble( st ->
                        st.getDayValues()
                          .stream()
                          .mapToDouble( DayValue::value )
                          .max()
                          .getAsDouble() )
                .max()
                .getAsDouble() ;
    }
}
