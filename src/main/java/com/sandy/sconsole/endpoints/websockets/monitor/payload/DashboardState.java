package com.sandy.sconsole.endpoints.websockets.monitor.payload;

import com.sandy.sconsole.core.util.ColorUtil;
import com.sandy.sconsole.state.ActiveTopicStatistics;
import com.sandy.sconsole.state.manager.ActiveTopicStatisticsManager;
import com.sandy.sconsole.state.manager.TodaySessionStatistics;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
class TopicState {

    private final int topicId ;
    private final String topicName ;
    private final int currentBurnRate ;
    private final int requiredBurnRate ;
    private final int numProblemsSolvedToday ;
    private final int numPigeons ;
    private final double burnStressScore ;
    private final String burnStressZone ;
    private final String burnStressZoneColor ;

    TopicState( ActiveTopicStatistics ts ) {
        this.topicId = ts.getTopic().getId() ;
        this.topicName = ts.getTopic().getTopicName() ;
        this.currentBurnRate = ts.getCurrentBurnRate() ;
        this.requiredBurnRate = ts.getRequiredBurnRate() ;
        this.numProblemsSolvedToday = ts.getNumProblemsSolvedToday() ;
        this.numPigeons = ts.getNumPigeonedProblems() ;
        this.burnStressScore = ts.getBurnStressScore() ;
        this.burnStressZone = ts.getBurnStressScoreLabel() ;
        this.burnStressZoneColor = ColorUtil.toHtmlColor( ts.getBurnStressScoreColor() ) ;
    }
}

class SyllabusState {

    @Getter private final List<TopicState> topicStates = new ArrayList<>() ;
    @Getter private final String syllabusName ;
    @Getter private final int duration ;
    
    SyllabusState( String syllabusName,
                   List<ActiveTopicStatistics> topicStats,
                   int duration ) {
        this.syllabusName = syllabusName ;
        this.duration = duration ;
        topicStats.forEach( topicStat -> {
            if( topicStat.isCurrentlyActive() ) {
                topicStates.add( new TopicState( topicStat ) ) ;
            }
        } ) ;
    }
}

public class DashboardState {

    @Getter final private List<SyllabusState> syllabusStates = new ArrayList<>() ;
    @Getter final private int totalDuration ;
    
    public DashboardState( ActiveTopicStatisticsManager atsMgr,
                           TodaySessionStatistics todayStats ) {
        
        this.totalDuration = todayStats.getTotalEffectiveTimeInSec() ;
        
        atsMgr.getSyllabusNames().forEach( syllabusName -> {
            List<ActiveTopicStatistics> atsList = atsMgr.getTopicStatistics( syllabusName ) ;
            int duration = todayStats.getSyllabusTime( syllabusName ) ;
            syllabusStates.add( new SyllabusState( syllabusName, atsList, duration ) ) ;
        }) ;
        syllabusStates.sort( Comparator.comparing( SyllabusState::getSyllabusName ) ) ;
    }
}
