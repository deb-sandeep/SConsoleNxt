package com.sandy.sconsole.state;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.dao.session.repo.DayStudyTimeRepo;
import com.sandy.sconsole.state.manager.TodayStudyStatistics;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TotalPastEffortProvider extends PastEffortProvider {
    
    private final DayStudyTimeRepo     studyTimeRepo ;
    private final TodayStudyStatistics todayStudyStatistics ;
    
    public TotalPastEffortProvider() {
        super( 60 ) ;
        this.studyTimeRepo = SConsole.getBean( DayStudyTimeRepo.class ) ;
        this.todayStudyStatistics = SConsole.getBean( TodayStudyStatistics.class ) ;
    }
    
    @Override
    protected Map<Date, Integer> getPastStudyTimes( int numPastDays ) {
        Map<Date, Integer> studyTimes = new HashMap<>() ;
        studyTimeRepo.getStudyTimesFromDate( startDate )
                     .forEach( studyTime -> {
                          studyTimes.put( studyTime.getDate(),
                                          studyTime.getTotalTime().intValue() ) ;
                     } ) ;
        return studyTimes ;
    }
    
    @Override
    protected int getTodayTime() {
        return todayStudyStatistics.getTotalEffectiveTimeInSec() ;
    }
}
