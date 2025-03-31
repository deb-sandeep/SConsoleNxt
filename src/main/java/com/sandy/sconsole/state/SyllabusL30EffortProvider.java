package com.sandy.sconsole.state;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.util.LastNDayValueProvider;
import com.sandy.sconsole.dao.session.repo.DaySyllabusStudyTimeRepo;
import com.sandy.sconsole.state.manager.TodayStudyStatistics;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SyllabusL30EffortProvider extends LastNDayValueProvider {
    
    @Getter private final String syllabusName ;
    
    private final DaySyllabusStudyTimeRepo studyTimeRepo ;
    private final TodayStudyStatistics todayStudyStatistics ;
    
    public SyllabusL30EffortProvider( String syllabusName ) {
        super( 30 ) ;
        this.syllabusName = syllabusName ;
        this.studyTimeRepo = SConsole.getBean( DaySyllabusStudyTimeRepo.class ) ;
        this.todayStudyStatistics = SConsole.getBean( TodayStudyStatistics.class ) ;
    }
    
    @Override
    protected Map<Date, Double> getPastDayValues( int numPastDays ) {
        Map<Date, Double> studyTimes = new HashMap<>() ;
        studyTimeRepo.getStudyTimesFromDate( startDate, syllabusName )
                .forEach( studyTime -> {
                    studyTimes.put( studyTime.getId().getDate(),
                                    (double)studyTime.getTotalTime().intValue()/3600 ) ;
                } ) ;
        return studyTimes ;
    }
    
    @Override
    protected double getTodayValue() {
        return (double)todayStudyStatistics.getSyllabusTime( syllabusName )/3600 ;
    }
}
