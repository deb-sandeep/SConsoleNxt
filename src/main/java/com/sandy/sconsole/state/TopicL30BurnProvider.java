package com.sandy.sconsole.state;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.util.LastNDayValueProvider;
import com.sandy.sconsole.dao.session.repo.ProblemAttemptRepo;
import com.sandy.sconsole.state.manager.TodayStudyStatistics;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TopicL30BurnProvider extends LastNDayValueProvider {
    
    @Getter private final int topicId    ;
    
    private final TodayStudyStatistics todayStudyStatistics ;
    private final ProblemAttemptRepo problemAttemptRepo ;
    
    public TopicL30BurnProvider( int topicId, ProblemAttemptRepo paRepo ) {
        super( 30 ) ;
        this.topicId = topicId ;
        this.problemAttemptRepo = paRepo ;
        this.todayStudyStatistics = SConsole.getBean( TodayStudyStatistics.class ) ;
    }
    
    @Override
    protected Map<Date, Double> getPastDayValues( int numPastDays ) {
        Map<Date, Double> dayBurnMap = new HashMap<>() ;
        problemAttemptRepo.getHistoricBurns( startDate, topicId )
                .forEach( dayBurn -> {
                    dayBurnMap.put( dayBurn.getDate(),
                                    (double)dayBurn.getNumQuestionsSolved() ) ;
                } ) ;
        return dayBurnMap ;
    }

    @Override
    protected double getTodayValue() {
        return todayStudyStatistics.getNumProblemsSolvedToday( topicId ) ;
    }
}
