package com.sandy.sconsole.state.manager;

import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.bus.EventTargetMarker;
import com.sandy.sconsole.dao.session.DailyBurnLog;
import com.sandy.sconsole.dao.session.DailyBurnLogId;
import com.sandy.sconsole.dao.session.repo.DailyBurnLogRepo;
import com.sandy.sconsole.state.ActiveTopicStatistics;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;

import static com.sandy.sconsole.EventCatalog.ATS_MANAGER_REFRESHED;
import static com.sandy.sconsole.EventCatalog.ATS_REFRESHED;

/**
 * Persists a live, per-topic snapshot of burn rate statistics into
 * daily_burn_log, driven by the same events that already drive the in-memory
 * ActiveTopicStatistics cache - not a nightly/end-of-day batch job. This
 * avoids relying on ClockTickListener day-tick ordering (listener
 * notification order across DAYS listeners is unspecified), since a row for
 * "today" is created/refreshed every time ActiveTopicStatisticsManager
 * recomputes a topic's stats (i.e. on every problem attempt end), and seeded
 * for every active topic whenever the manager does a full refresh.
 */
@Slf4j
@Component
@DependsOn( { "eventBus" } )
public class DailyBurnLogWriter implements EventSubscriber {

    @Autowired private EventBus eventBus ;
    @Autowired private ActiveTopicStatisticsManager atsManager ;
    @Autowired private DailyBurnLogRepo repo ;

    @PostConstruct
    public void init() {
        eventBus.addSyncSubscriber( this, ATS_MANAGER_REFRESHED, ATS_REFRESHED ) ;
        // Covers the very first startup - Spring does not guarantee that
        // this bean's @PostConstruct runs before ActiveTopicStatisticsManager's,
        // so the initial ATS_MANAGER_REFRESHED publish could otherwise be missed.
        upsertAllActiveTopics() ;
    }

    @Override
    public synchronized void handleEvent( Event event ) {
        switch( event.getEventId() ) {
            case ATS_MANAGER_REFRESHED -> upsertAllActiveTopics() ;
            case ATS_REFRESHED -> upsertTopic( (Integer)event.getValue() ) ;
        }
    }

    @EventTargetMarker( ATS_MANAGER_REFRESHED )
    private void upsertAllActiveTopics() {
        atsManager.getAllActiveTopicStatistics().forEach( this::upsert ) ;
    }

    @EventTargetMarker( ATS_REFRESHED )
    private void upsertTopic( int topicId ) {
        ActiveTopicStatistics ats = atsManager.getTopicStatistics( topicId ) ;
        if( ats != null ) {
            upsert( ats ) ;
        }
    }

    private void upsert( ActiveTopicStatistics ats ) {

        Date today = DateUtils.truncate( new Date(), Calendar.DATE ) ;
        int topicId = ats.getTopicId() ;

        int originalBurnRate = ats.getOriginalBurnRate() ;
        int currentBurnRate  = ats.getCurrentBurnRate() ;
        int requiredBurnRate = ats.getRequiredBurnRate() ;
        int todayBurn        = ats.getNumProblemsSolvedToday() ;

        boolean originalBurnMet = todayBurn >= originalBurnRate ;
        boolean currentBurnMet  = todayBurn >= currentBurnRate ;
        boolean requiredBurnMet = todayBurn >= requiredBurnRate ;

        BigDecimal requiredBurnExceedPct = null ;
        if( requiredBurnRate > 0 ) {
            requiredBurnExceedPct = BigDecimal.valueOf( todayBurn - requiredBurnRate )
                    .multiply( BigDecimal.valueOf( 100 ) )
                    .divide( BigDecimal.valueOf( requiredBurnRate ), 2, RoundingMode.HALF_UP ) ;
        }

        DailyBurnLogId id = new DailyBurnLogId( today, topicId ) ;
        DailyBurnLog entry = repo.findById( id ).orElseGet( DailyBurnLog::new ) ;

        // A coach-set override (see toggleBurnMetOverride()) makes the day
        // count as met for streak purposes even if the mechanical
        // requiredBurnMet threshold isn't reached. Read it off the entry
        // fetched above - before any of this method's own field writes -
        // so a subsequent live recompute (e.g. another problem solved later
        // the same day) doesn't silently discard an earlier override. Then
        // write the normalized value straight back: for an existing row
        // this is a no-op, but for a brand new row (entry.getBurnMetOverride()
        // is null, since DailyBurnLog::new leaves it unset) it seeds the
        // not-null column with its default of false.
        boolean burnMetOverride = Boolean.TRUE.equals( entry.getBurnMetOverride() ) ;
        entry.setBurnMetOverride( burnMetOverride ) ;
        boolean effectiveBurnMet = requiredBurnMet || burnMetOverride ;

        Date yesterday = DateUtils.addDays( today, -1 ) ;
        int prevStreak = repo.findById( new DailyBurnLogId( yesterday, topicId ) )
                              .map( DailyBurnLog::getStreakCount )
                              .orElse( 0 ) ;
        int streakCount = effectiveBurnMet ? prevStreak + 1 : 0 ;

        entry.setId( id ) ;
        entry.setOriginalBurnRate( originalBurnRate ) ;
        entry.setCurrentBurnRate( currentBurnRate ) ;
        entry.setRequiredBurnRate( requiredBurnRate ) ;
        entry.setTodayBurn( todayBurn ) ;
        entry.setOriginalBurnMet( originalBurnMet ) ;
        entry.setCurrentBurnMet( currentBurnMet ) ;
        entry.setRequiredBurnMet( requiredBurnMet ) ;
        entry.setRequiredBurnExceedPct( requiredBurnExceedPct ) ;
        entry.setStreakCount( streakCount ) ;

        repo.save( entry ) ;
    }

    /**
     * Flips burn_met_override on today's row for the given topic, pushes the
     * new value directly onto the live ActiveTopicStatistics instance (this
     * class already holds a reference to it via atsManager, so there's no
     * need to round-trip through an event/DB-reread to bring ATS up to
     * date), then re-derives streakCount via upsert() so the flip takes
     * effect immediately rather than waiting for the next live burn event.
     */
    public synchronized void toggleBurnMetOverride( int topicId ) {

        Date today = DateUtils.truncate( new Date(), Calendar.DATE ) ;
        DailyBurnLogId id = new DailyBurnLogId( today, topicId ) ;
        DailyBurnLog entry = repo.findById( id )
                .orElseThrow( () -> new IllegalStateException(
                        "No daily_burn_log row for topic " + topicId + " on " + today ) ) ;

        boolean newOverride = !Boolean.TRUE.equals( entry.getBurnMetOverride() ) ;
        entry.setBurnMetOverride( newOverride ) ;
        repo.save( entry ) ;

        ActiveTopicStatistics ats = atsManager.getTopicStatistics( topicId ) ;
        if( ats != null ) {
            ats.setBurnMetOverride( newOverride ) ;
            upsert( ats ) ;
        }
    }
}
