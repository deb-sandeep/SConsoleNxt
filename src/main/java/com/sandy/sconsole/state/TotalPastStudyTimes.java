package com.sandy.sconsole.state;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.dao.session.repo.DayStudyTimeRepo;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TotalPastStudyTimes extends PastStudyTimes {
    
    private final DayStudyTimeRepo studyTimeRepo ;
    
    public TotalPastStudyTimes() {
        super( 60 ) ;
        this.studyTimeRepo = SConsole.getBean( DayStudyTimeRepo.class ) ;
        super.init() ;
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
        return 0;
    }
}
