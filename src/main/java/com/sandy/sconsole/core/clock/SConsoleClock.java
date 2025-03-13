package com.sandy.sconsole.core.clock;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.sandy.sconsole.core.log.LogIndenter;
import it.sauronsoftware.cron4j.Scheduler;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jfree.data.time.Day;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.sandy.sconsole.core.log.LogIndenter.THREAD_NAME_KEY;

@Slf4j
@Component
public class SConsoleClock {

    public static class CurrentTimeProvider {
        public Calendar getCurrentTime() {
            return Calendar.getInstance() ;
        }
    }

    private final Timer SEC_TIMER = new Timer("SEC_TIMER", true ) ;
    private final Object lock = new Object() ;

    @Getter private Day today ;
    @Getter private long todayFirstMillis ;
    @Getter private long todayLastMillis ;
    @Getter private Timestamp todayFirstTimestamp ;
    @Getter private Timestamp todayLastTimestamp ;

    @Setter private CurrentTimeProvider currentTimeProvider = new CurrentTimeProvider() ;

    private final SetMultimap<TimeUnit, ClockTickListener> tickListeners = HashMultimap.create() ;
    
    private final Scheduler scheduler = new Scheduler() ;

    public void initialize() {

        computeTodayTimeMarkers() ;

        log.debug( "-> Starting the Scheduler." );
        this.scheduler.start() ;

        log.debug( "-> Starting the per second timer." );
        SEC_TIMER.scheduleAtFixedRate( new TimerTask() {

            Calendar lastDate = null;

            public void run() {

                try {
                    MDC.put( THREAD_NAME_KEY, "clockDaemon" ) ;
                    Calendar now = currentTimeProvider.getCurrentTime() ;
                    synchronized ( lock ) {

                        notifyTickListeners( TimeUnit.SECONDS, now ) ;
                        if( lastDate != null ) {
                            // A new minute has begum
                            if( now.get(Calendar.MINUTE) !=
                                    lastDate.get(Calendar.MINUTE)) {
                                notifyTickListeners( TimeUnit.MINUTES, now ) ;
                            }

                            // A new hour has begum
                            if( now.get(Calendar.HOUR_OF_DAY) !=
                                    lastDate.get(Calendar.HOUR_OF_DAY)) {
                                notifyTickListeners( TimeUnit.HOURS, now ) ;
                            }

                            // A new day has begum
                            if( now.get(Calendar.DAY_OF_YEAR) !=
                                    lastDate.get(Calendar.DAY_OF_YEAR)) {
                                computeTodayTimeMarkers() ;
                                notifyTickListeners( TimeUnit.DAYS, now ) ;
                            }
                        }
                        lastDate = now ;
                    }
                }
                finally {
                    LogIndenter.resetIndent() ;
                    MDC.remove( THREAD_NAME_KEY ) ;
                }
            }
        }, new Date(), 1000 ) ;
    }

    public void stopClock() {
        SEC_TIMER.cancel() ;
        if( scheduler.isStarted() ) {
            scheduler.stop() ;
        }
    }
    
    public void addTickListener( ClockTickListener tickListener, TimeUnit timeUnit, TimeUnit ...additionalTimeUnits ) {
        synchronized ( lock ) {
            addTickListenerForSingleTimeUnit( tickListener, timeUnit ) ;
            for( TimeUnit tu : additionalTimeUnits ) {
                addTickListenerForSingleTimeUnit( tickListener, tu ) ;
            }
        }
    }

    public void addTickListenerForSingleTimeUnit( ClockTickListener l, TimeUnit unit ) {
        if( !( unit == TimeUnit.SECONDS ||
               unit == TimeUnit.MINUTES ||
               unit == TimeUnit.DAYS ||
               unit == TimeUnit.HOURS ) ) {
            throw new IllegalArgumentException( "Currently clock only " +
                    "supports tick listeners for SECONDS, MINUTES, HOURS and DAYS." ) ;
        }

        synchronized( lock ) {
            if( tickListeners.get( unit ).contains( l ) ) {
                throw new IllegalArgumentException( "ClockTickListener is already registered." ) ;
            }
            tickListeners.put( unit, l ) ;
        }
    }

    public void removeTickListener( @NonNull ClockTickListener l ) {
        synchronized( lock ) {
            TimeUnit[] keys = tickListeners.keySet().toArray( new TimeUnit[0] ) ;
            for( TimeUnit tu : keys ) {
                tickListeners.remove( tu, l ) ;
            }
        }
    }
    
    public void removeTickListener( TimeUnit unit, @NonNull ClockTickListener l ) {
        synchronized( lock ) {
            tickListeners.remove( unit, l ) ;
        }
    }

    /**
     * Schedules a task as per the scheduling expression.
     *
     * @param scheduleExpr A cron expression. For details see
     *     <a href="https://www.sauronsoftware.it/projects/cron4j/manual.php#p02">Patterns</a>
     * @return A unique identifier which can be used to deschedule or reschedule
     *      the task.
     */
    public String scheduleTask( String scheduleExpr, ScheduledTask task ) {
        return this.scheduler.schedule( scheduleExpr, task ) ;
    }

    public void descheduleTask( String id ) {
        this.scheduler.deschedule( id ) ;
    }

    private void notifyTickListeners( TimeUnit timeUnit, Calendar now ) {

        synchronized( lock ) {
            Set<ClockTickListener> listeners = tickListeners.get( timeUnit ) ;
            
            // Do not replace with enhanced for loop or iterator. The
            // listener list can be modified during the notification cycle.
            listeners.iterator().forEachRemaining( listener -> {
                try {
                    if( listener.isAsync() ) {
                        Map<String, String> mdcCtxMap = MDC.getCopyOfContextMap() ;
                        new Thread( () -> {
                            try {
                                MDC.setContextMap( mdcCtxMap ) ;
                                callTickNotificationMethod( timeUnit, now, listener ) ;
                            }
                            catch( Exception e ) {
                                log.error( "Exception in processing async clock tick.", e ) ;
                            }
                        }).start() ;
                    }
                    else {
                        callTickNotificationMethod( timeUnit, now, listener ) ;
                    }
                }
                catch( Exception e ) {
                    log.error( "Exception in processing clock tick.", e ) ;
                }
            } ) ;
        }
    }
    
    private void callTickNotificationMethod( TimeUnit timeUnit, Calendar now,
                                             ClockTickListener tickListener ) {
        switch( timeUnit ) {
            case DAYS -> tickListener.dayTicked( now ) ;
            case HOURS -> tickListener.hourTicked( now ) ;
            case MINUTES -> tickListener.minuteTicked( now ) ;
            case SECONDS -> tickListener.secondTicked( now ) ;
        }
    }

    private void computeTodayTimeMarkers() {

        today               = new Day( new Date() ) ;
        todayFirstMillis    = today.getFirstMillisecond() ;
        todayLastMillis     = today.getLastMillisecond() ;
        todayFirstTimestamp = new Timestamp( todayFirstMillis ) ;
        todayLastTimestamp  = new Timestamp( todayLastMillis ) ;
    }
}
