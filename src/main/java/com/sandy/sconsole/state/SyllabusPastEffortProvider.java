package com.sandy.sconsole.state;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.dao.session.repo.DaySyllabusStudyTimeRepo;
import com.sandy.sconsole.state.manager.TodayStudyStatistics;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SyllabusPastEffortProvider extends LastNDayEffortProvider {
    
    @Getter private final String syllabusName ;
    
    private final DaySyllabusStudyTimeRepo studyTimeRepo ;
    private final TodayStudyStatistics     todayStudyStatistics ;
    
    public SyllabusPastEffortProvider( String syllabusName ) {
        super( 30 ) ;
        this.syllabusName = syllabusName ;
        this.studyTimeRepo = SConsole.getBean( DaySyllabusStudyTimeRepo.class ) ;
        this.todayStudyStatistics = SConsole.getBean( TodayStudyStatistics.class ) ;
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
        return todayStudyStatistics.getSyllabusTime( syllabusName ) ;
    }
}
