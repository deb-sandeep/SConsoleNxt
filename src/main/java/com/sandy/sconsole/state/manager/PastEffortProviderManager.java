package com.sandy.sconsole.state.manager;

import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.bus.EventTargetMarker;
import com.sandy.sconsole.core.clock.ClockTickListener;
import com.sandy.sconsole.core.clock.SConsoleClock;
import com.sandy.sconsole.core.util.DayValue;
import com.sandy.sconsole.core.util.LastNDayValueProvider;
import com.sandy.sconsole.state.SyllabusL30EffortProvider;
import com.sandy.sconsole.state.TotalL60PastEffortProvider;
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
import static com.sandy.sconsole.EventCatalog.*;

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
    
    @Autowired private SConsoleClock clock ;
    @Autowired private EventBus eventBus ;
    
    private final LastNDayValueProvider totalPastEffortProvider = new TotalL60PastEffortProvider();
    
    private final LastNDayValueProvider[] pastEffortProviders = {
       new SyllabusL30EffortProvider( IIT_PHY_SYLLABUS_NAME ),
       new SyllabusL30EffortProvider( IIT_CHEM_SYLLABUS_NAME ),
       new SyllabusL30EffortProvider( IIT_MATHS_SYLLABUS_NAME ),
       new SyllabusL30EffortProvider( REASONING_SYLLABUS_NAME ),
       totalPastEffortProvider
    } ;
    
    private final Map<String, LastNDayValueProvider> syllabusPastEffortProvidersMap = new HashMap<>() ;
    
    public PastEffortProviderManager() {
        Arrays.stream( pastEffortProviders ).forEach( provider -> {
            if( provider instanceof SyllabusL30EffortProvider ) {
                syllabusPastEffortProvidersMap.put( (( SyllabusL30EffortProvider )provider).getSyllabusName(), provider ) ;
            }
        }) ;
    }
    
    @PostConstruct
    public void init() {
        clock.addTickListener( this, TimeUnit.DAYS ) ;
        eventBus.addAsyncSubscriber( this, HISTORIC_SESSION_UPDATED );
        eventBus.addAsyncSubscriber( this, TODAY_EFFORT_UPDATED );
        fullRefresh() ;
    }
    
    @Override
    public void dayTicked( Calendar date ) {
        fullRefresh() ;
    }
    
    @Override
    public synchronized void handleEvent( Event event ) {
        int eventType = event.getEventId() ;
        switch ( eventType ) {
            case HISTORIC_SESSION_UPDATED -> fullRefresh() ;
            case TODAY_EFFORT_UPDATED -> updateTodayTime() ;
        }
    }
    
    public SyllabusL30EffortProvider getPastEffortProvider( String syllabusName ) {
        return ( SyllabusL30EffortProvider )syllabusPastEffortProvidersMap.get( syllabusName );
    }
    
    public LastNDayValueProvider getPastEffortProvider() {
        return totalPastEffortProvider;
    }
    
    @EventTargetMarker( HISTORIC_SESSION_UPDATED )
    private void fullRefresh() {
        Arrays.stream( pastEffortProviders ).forEach( LastNDayValueProvider::fullRefresh ) ;
        eventBus.publishEvent( PAST_EFFORT_UPDATED ) ;
    }
    
    @EventTargetMarker( TODAY_EFFORT_UPDATED )
    private void updateTodayTime() {
        Arrays.stream( pastEffortProviders ).forEach( LastNDayValueProvider::updateTodayValue ) ;
        eventBus.publishEvent( PAST_EFFORT_UPDATED ) ;
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
