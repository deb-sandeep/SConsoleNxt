package com.sandy.sconsole.endpoints.rest.live.problem ;

import com.sandy.sconsole.core.api.AR ;
import com.sandy.sconsole.dao.session.repo.ProblemAttemptRepo ;
import com.sandy.sconsole.endpoints.rest.live.problem.vo.ActiveTopicChartVO ;
import com.sandy.sconsole.endpoints.rest.live.problem.vo.ActiveTopicChartVO.BurnPoint ;
import com.sandy.sconsole.endpoints.rest.live.problem.vo.ActiveTopicChartVO.PlanMetrics ;
import com.sandy.sconsole.endpoints.rest.live.problem.vo.ActiveTopicChartVO.StatusMetrics ;
import com.sandy.sconsole.endpoints.rest.live.problem.vo.ActiveTopicChartVO.TopicInfo ;
import com.sandy.sconsole.state.ActiveTopicStatistics ;
import com.sandy.sconsole.state.manager.ActiveTopicStatisticsManager ;
import lombok.extern.slf4j.Slf4j ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList ;
import java.util.Calendar ;
import java.util.Date ;
import java.util.List ;

import static com.sandy.sconsole.core.api.AR.*;

@Slf4j
@RestController
@RequestMapping( "/Topic" )
public class ActiveTopicChartAPI {

    @Autowired
    private ActiveTopicStatisticsManager activeTopicStatsMgr ;

    @GetMapping( "/{topicId}/burnChart" )
    public ResponseEntity<AR<ActiveTopicChartVO>> getBurnChart( @PathVariable int topicId ) {
        try {
            ActiveTopicStatistics ats = activeTopicStatsMgr.getTopicStatistics( topicId ) ;
            if( ats == null ) {
                return functionalError( "Topic not found: " + topicId ) ;
            }
            return success( buildVO( ats ) ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    // -----------------------------------------------------------------------

    private ActiveTopicChartVO buildVO( ActiveTopicStatistics ats ) {
        ActiveTopicChartVO vo = new ActiveTopicChartVO() ;
        vo.setTopic(  buildTopicInfo( ats ) ) ;
        vo.setPlan(   buildPlanMetrics( ats ) ) ;
        vo.setStatus( buildStatusMetrics( ats ) ) ;
        List<BurnPoint> actual = buildActualBurn( ats ) ;
        vo.setActualBurn( actual ) ;
        vo.setIdealBurn( buildIdealBurn( ats ) ) ;
        vo.setProjectedBurn( buildProjectedBurn( ats, actual ) ) ;
        return vo ;
    }

    private TopicInfo buildTopicInfo( ActiveTopicStatistics ats ) {
        TopicInfo ti = new TopicInfo() ;
        ti.setTopicId(      ats.getTopic().getId() ) ;
        ti.setSyllabusName( ats.getTopic().getSyllabusName() ) ;
        ti.setSectionName(  ats.getTopic().getSectionName() ) ;
        ti.setTopicName(    ats.getTopic().getTopicName() ) ;
        return ti ;
    }

    private PlanMetrics buildPlanMetrics( ActiveTopicStatistics ats ) {
        PlanMetrics pm = new PlanMetrics() ;
        pm.setStartDate(         ats.getStartDate() ) ;
        pm.setCoachingEndDate(   ats.getCoachingNumDays()  > 0 ? ats.getCoachingEndDate()  : null ) ;
        pm.setSelfStudyEndDate(  ats.getSelfStudyNumDays() > 0 ? ats.getSelfStudyEndDate() : null ) ;
        pm.setExerciseStartDate( ats.getExerciseStartDate() ) ;
        pm.setExerciseEndDate(   ats.getExerciseEndDate() ) ;
        pm.setEndDate(           ats.getEndDate() ) ;
        pm.setNumTotalProblems(  ats.getNumTotalProblems() ) ;
        pm.setNumExerciseDays(   ats.getNumExerciseDays() ) ;
        pm.setOriginalBurnRate(  ats.getOriginalBurnRate() ) ;
        return pm ;
    }

    private StatusMetrics buildStatusMetrics( ActiveTopicStatistics ats ) {
        StatusMetrics sm = new StatusMetrics() ;
        sm.setCurrentZone(       ats.getCurrentZone().name() ) ;
        sm.setNumProblemsLeft(   ats.getNumProblemsLeft() ) ;
        sm.setCurrentBurnRate(   ats.getCurrentBurnRate() ) ;
        sm.setRequiredBurnRate(  ats.getRequiredBurnRate() ) ;
        sm.setBurnStressScore(   ats.getBurnStressScore() ) ;
        sm.setScoreLabel(        ats.getBurnStressScoreLabel() ) ;
        sm.setNumOvershootDays(  ats.getNumOvershootDays() ) ;
        sm.setLeadLagProblems(   ats.getLeadLagProblems() ) ;
        sm.setNumExerciseDaysLeft( ats.getNumExerciseDaysLeft() ) ;
        return sm ;
    }

    private List<BurnPoint> buildActualBurn( ActiveTopicStatistics ats ) {
        List<BurnPoint> series = new ArrayList<>() ;
        List<ProblemAttemptRepo.DayBurn> burns = ats.getHistoricBurns() ;
        if( burns.isEmpty() ) return series ;

        // Synthetic anchor: day before first solved day, Y = numTotalProblems
        series.add( new BurnPoint( dayBefore( burns.get(0).getDate() ), ats.getNumTotalProblems() ) ) ;

        int  remaining = ats.getNumTotalProblems() ;
        Date lastDate  = null ;
        for( ProblemAttemptRepo.DayBurn db : burns ) {
            remaining -= db.getNumQuestionsSolved() ;
            series.add( new BurnPoint( db.getDate(), remaining ) ) ;
            lastDate = db.getDate() ;
        }

        // Extend the line to today if the topic is still in progress
        Date today = truncateToDay( new Date() ) ;
        if( lastDate != null && today.after( lastDate ) && !today.after( ats.getEndDate() ) ) {
            series.add( new BurnPoint( today, ats.getNumProblemsLeft() ) ) ;
        }
        return series ;
    }

    // Only inflection points: Chart.js interpolates straight lines between them
    private List<BurnPoint> buildIdealBurn( ActiveTopicStatistics ats ) {
        List<BurnPoint> series = new ArrayList<>() ;
        int total = ats.getNumTotalProblems() ;

        series.add( new BurnPoint( ats.getStartDate(), total ) ) ;
        if( !sameDay( ats.getExerciseStartDate(), ats.getStartDate() ) ) {
            series.add( new BurnPoint( ats.getExerciseStartDate(), total ) ) ;
        }
        series.add( new BurnPoint( ats.getExerciseEndDate(), 0 ) ) ;
        if( !sameDay( ats.getEndDate(), ats.getExerciseEndDate() ) ) {
            series.add( new BurnPoint( ats.getEndDate(), 0 ) ) ;
        }
        return series ;
    }

    private List<BurnPoint> buildProjectedBurn( ActiveTopicStatistics ats,
                                                List<BurnPoint> actualBurn ) {
        List<BurnPoint> series = new ArrayList<>() ;
        if( actualBurn.isEmpty() || ats.getCurrentBurnRate() <= 0 ) return series ;

        BurnPoint last = actualBurn.get( actualBurn.size() - 1 ) ;
        series.add( last ) ;

        int  remaining = last.getRemaining() ;
        int  rate      = ats.getCurrentBurnRate() ;
        Date endDate   = ats.getEndDate() ;

        Calendar cal = Calendar.getInstance() ;
        cal.setTime( last.getDate() ) ;
        cal.add( Calendar.DAY_OF_YEAR, 1 ) ;

        while( remaining > 0 && !cal.getTime().after( endDate ) ) {
            remaining = Math.max( 0, remaining - rate ) ;
            series.add( new BurnPoint( truncateToDay( cal.getTime() ), remaining ) ) ;
            if( remaining == 0 ) break ;
            cal.add( Calendar.DAY_OF_YEAR, 1 ) ;
        }
        return series ;
    }

    // -----------------------------------------------------------------------

    private static Date dayBefore( Date date ) {
        Calendar cal = Calendar.getInstance() ;
        cal.setTime( date ) ;
        cal.add( Calendar.DAY_OF_YEAR, -1 ) ;
        cal.set( Calendar.HOUR_OF_DAY, 0 ) ;
        cal.set( Calendar.MINUTE,      0 ) ;
        cal.set( Calendar.SECOND,      0 ) ;
        cal.set( Calendar.MILLISECOND, 0 ) ;
        return cal.getTime() ;
    }

    private static Date truncateToDay( Date date ) {
        Calendar cal = Calendar.getInstance() ;
        cal.setTime( date ) ;
        cal.set( Calendar.HOUR_OF_DAY, 0 ) ;
        cal.set( Calendar.MINUTE,      0 ) ;
        cal.set( Calendar.SECOND,      0 ) ;
        cal.set( Calendar.MILLISECOND, 0 ) ;
        return cal.getTime() ;
    }

    private static boolean sameDay( Date a, Date b ) {
        return truncateToDay( a ).equals( truncateToDay( b ) ) ;
    }
}
