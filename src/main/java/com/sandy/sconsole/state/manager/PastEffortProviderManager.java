package com.sandy.sconsole.state.manager;

import com.sandy.sconsole.EventCatalog;
import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.clock.ClockTickListener;
import com.sandy.sconsole.core.clock.SConsoleClock;
import com.sandy.sconsole.core.util.DayValue;
import com.sandy.sconsole.state.LastNDayEffortProvider;
import com.sandy.sconsole.state.SyllabusPastEffortProvider;
import com.sandy.sconsole.state.TotalPastEffortProvider;
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

import static com.sandy.sconsole.AppConstants.*;

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
public class PastEffortProviderManager implements ClockTickListener, EventSubscriber {
    
    private static final int[] SUBSCRIBED_EVENTS = {
            EventCatalog.HISTORIC_SESSION_UPDATED,
            EventCatalog.TODAY_EFFORT_UPDATED,
    } ;
    
    @Autowired private SConsoleClock clock ;
    @Autowired private EventBus eventBus ;
    
    private final LastNDayEffortProvider totalPastEffortProvider = new TotalPastEffortProvider();
    
    private final LastNDayEffortProvider[] pastEffortProviders = {
       new SyllabusPastEffortProvider( IIT_PHY_SYLLABUS_NAME ),
       new SyllabusPastEffortProvider( IIT_CHEM_SYLLABUS_NAME ),
       new SyllabusPastEffortProvider( IIT_MATHS_SYLLABUS_NAME ),
       new SyllabusPastEffortProvider( REASONING_SYLLABUS_NAME ),
       totalPastEffortProvider
    } ;
    
    private final Map<String, LastNDayEffortProvider> syllabusPastEffortProvidersMap = new HashMap<>() ;
    
    public PastEffortProviderManager() {
        Arrays.stream( pastEffortProviders ).forEach( provider -> {
            if( provider instanceof SyllabusPastEffortProvider ) {
                syllabusPastEffortProvidersMap.put( (( SyllabusPastEffortProvider )provider).getSyllabusName(), provider ) ;
            }
        }) ;
    }
    
    @PostConstruct
    public void init() {
        clock.addTickListener( this, TimeUnit.DAYS ) ;
        eventBus.addSubscriber( this, true, SUBSCRIBED_EVENTS ) ;
        fullRefresh() ;
    }
    
    @Override
    public void dayTicked( Calendar date ) {
        fullRefresh() ;
    }
    
    @Override
    public void handleEvent( Event event ) {
        int eventType = event.getEventId() ;
        switch ( eventType ) {
            case EventCatalog.HISTORIC_SESSION_UPDATED -> fullRefresh() ;
            case EventCatalog.TODAY_EFFORT_UPDATED -> updateTodayTime() ;
        }
    }
    
    public SyllabusPastEffortProvider getPastEffortProvider( String syllabusName ) {
        return ( SyllabusPastEffortProvider )syllabusPastEffortProvidersMap.get( syllabusName );
    }
    
    public LastNDayEffortProvider getPastEffortProvider() {
        return totalPastEffortProvider;
    }
    
    private void fullRefresh() {
        Arrays.stream( pastEffortProviders ).forEach( LastNDayEffortProvider::fullRefresh ) ;
        eventBus.publishEvent( EventCatalog.PAST_EFFORT_UPDATED ) ;
    }
    
    private void updateTodayTime() {
        Arrays.stream( pastEffortProviders ).forEach( LastNDayEffortProvider::updateTodayTime ) ;
        eventBus.publishEvent( EventCatalog.PAST_EFFORT_UPDATED ) ;
    }
    
    // Returns the max day value across the historic data of all syllabus
    public double getMaxSyllabusTime() {
        return syllabusPastEffortProvidersMap.values()
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
