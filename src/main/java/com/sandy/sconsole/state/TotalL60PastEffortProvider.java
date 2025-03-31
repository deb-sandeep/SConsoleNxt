package com.sandy.sconsole.state;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.util.LastNDayValueProvider;
import com.sandy.sconsole.dao.session.repo.DayStudyTimeRepo;
import com.sandy.sconsole.state.manager.TodayStudyStatistics;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TotalL60PastEffortProvider extends LastNDayValueProvider {
    
    private final DayStudyTimeRepo     studyTimeRepo ;
    private final TodayStudyStatistics todayStudyStatistics ;
    
    public TotalL60PastEffortProvider() {
        super( 60 ) ;
        this.studyTimeRepo = SConsole.getBean( DayStudyTimeRepo.class ) ;
        this.todayStudyStatistics = SConsole.getBean( TodayStudyStatistics.class ) ;
    }
    
    @Override
    protected Map<Date, Double> getPastDayValues( int numPastDays ) {
        Map<Date, Double> studyTimes = new HashMap<>() ;
        studyTimeRepo.getStudyTimesFromDate( startDate )
                     .forEach( studyTime -> {
                          studyTimes.put( studyTime.getDate(),
                                          (double)studyTime.getTotalTime().intValue()/3600 ) ;
                     } ) ;
        return studyTimes ;
    }
    
    @Override
    protected double getTodayValue() {
        return (double)todayStudyStatistics.getTotalEffectiveTimeInSec()/3600 ;
    }
}
