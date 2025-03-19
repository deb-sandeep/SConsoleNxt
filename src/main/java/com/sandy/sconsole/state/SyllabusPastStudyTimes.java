package com.sandy.sconsole.state;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.dao.session.repo.DaySyllabusStudyTimeRepo;
import lombok.Getter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SyllabusPastStudyTimes extends PastStudyTimes {
    
    @Getter private final String syllabusName ;
    
    private final DaySyllabusStudyTimeRepo studyTimeRepo ;
    
    public SyllabusPastStudyTimes( String syllabusName ) {
        super( 30 ) ;
        this.syllabusName = syllabusName ;
        this.studyTimeRepo = SConsole.getBean( DaySyllabusStudyTimeRepo.class ) ;
        super.init() ;
    }
    
    @Override
    protected Map<Date, Integer> getPastStudyTimes( int numPastDays ) {
        Map<Date, Integer> studyTimes = new HashMap<>() ;
        studyTimeRepo.getStudyTimesFromDate( startDate, syllabusName )
                     .forEach( studyTime -> {
                          studyTimes.put( studyTime.getId().getDate(),
                                          studyTime.getTotalTime().intValue() ) ;
                     } ) ;
        return studyTimes ;
    }
    
    @Override
    protected int getTodayTime() {
        return 0;
    }
}
