package com.sandy.sconsole.endpoints.websockets.monitor.payload;

import com.sandy.sconsole.state.ActiveTopicStatistics;
import com.sandy.sconsole.state.manager.ActiveTopicStatisticsManager;
import com.sandy.sconsole.state.manager.TodaySessionStatistics;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
class TopicState {
    
    private final String topicName ;
    private final int currentBurnRate ;
    private final int requiredBurnRate ;
    private final int numProblemsSolvedToday ;
    
    TopicState( ActiveTopicStatistics ts ) {
        this.topicName = ts.getTopic().getTopicName() ;
        this.currentBurnRate = ts.getCurrentBurnRate() ;
        this.requiredBurnRate = ts.getRequiredBurnRate() ;
        this.numProblemsSolvedToday = ts.getNumProblemsSolvedToday() ;
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
        topicStats.forEach( topicStat -> topicStates.add( new TopicState( topicStat ) ) ) ;
    }
}

public class DashboardState {

    @Getter final private List<SyllabusState> syllabusStates = new ArrayList<>() ;
    @Getter final private int totalDuration ;
    
    public DashboardState( ActiveTopicStatisticsManager atsMgr,
                           TodaySessionStatistics todayStats ) {
        
        this.totalDuration = todayStats.getTotalEffectiveTimeInSec() ;
        
        atsMgr.getActiveSyllabusNames().forEach( syllabusName -> {
            List<ActiveTopicStatistics> atsList = atsMgr.getTopicStatistics( syllabusName ) ;
            int duration = todayStats.getSyllabusTime( syllabusName ) ;
            syllabusStates.add( new SyllabusState( syllabusName, atsList, duration ) ) ;
        }) ;
        syllabusStates.sort( Comparator.comparing( SyllabusState::getSyllabusName ) ) ;
    }
}
