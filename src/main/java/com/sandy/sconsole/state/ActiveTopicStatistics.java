package com.sandy.sconsole.state;

import com.sandy.sconsole.dao.master.TopicTrackAssignment;
import com.sandy.sconsole.dao.master.dto.TopicVO;
import com.sandy.sconsole.dao.master.repo.TopicRepo;
import com.sandy.sconsole.dao.session.repo.DaySyllabusStudyTimeRepo;
import com.sandy.sconsole.dao.session.repo.TodaySolvedProblemCountRepo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.sandy.sconsole.core.util.SConsoleUtil.isBetween;

@Slf4j
@Component
@Scope( "prototype" )
public class ActiveTopicStatistics {
    
    public enum Zone { PRE_START, BUFFER_START, THEORY, EXERCISE, BUFFER_END, POST_END }
    
    @Autowired private TopicRepo topicRepo ;
    @Autowired private TodaySolvedProblemCountRepo tspcRepo ;
    @Autowired private DaySyllabusStudyTimeRepo studyTimeRepo ;
    
    @Getter private TopicVO topic ;
    
    private int topicId ;
    private int bufferLeft ;
    private int bufferRight ;
    private int theoryMargin ;
    
    @Getter private Date startDate ;
    @Getter private Date endDate ;
    
    @Getter private int  totalDurationDays ;
    @Getter private int  exerciseDurationDays ;
    @Getter private int  remainingExerciseDays ;
    @Getter private Date exerciseStartDate;
    @Getter private Date exerciseEndDate;
    
    @Getter private int totalProblemsCount ;
    @Getter private int remainingProblemCount ;
    
    @Getter private Zone currentZone ;
    @Getter private int originalBurnRate ;
    @Getter private int requiredBurnRate ;
    
    @Getter private int numProblemsSolvedToday = 0 ;
    
    private Map<Date, Integer> l30StudyTime = new LinkedHashMap<>() ;

    public ActiveTopicStatistics() {}
    
    void setTopicTrackAssignment( TopicTrackAssignment assignment ) {
        this.topicId      = assignment.getTopicId() ;
        this.bufferLeft   = assignment.getBufferLeft() ;
        this.bufferRight  = assignment.getBufferRight() ;
        this.theoryMargin = assignment.getTheoryMargin() ;
        this.startDate    = assignment.getStartDate() ;
        this.endDate      = assignment.getEndDate() ;
    }
    
    void init() {
        topic = new TopicVO( topicRepo.findById( topicId ).get() ) ;
        log.debug( "    Initializing active topic statistics for topic {}", topic.getTopicName() );
        refreshState() ;
    }
    
    void destroy() {}
    
    public int getCompletedProblemsCount() {
        return totalProblemsCount - remainingProblemCount ;
    }
    
    public void refreshState() {
        totalProblemsCount = topicRepo.getTotalProblemCount( topicId ) ;
        remainingProblemCount = topicRepo.getRemainingProblemCount( topicId ) ;
        
        Integer tempInt = tspcRepo.getNumSolvedProblems( topicId ) ;
        numProblemsSolvedToday = tempInt == null ? 0 : tempInt ;
        
        Duration duration    = Duration.between( startDate.toInstant(), endDate.toInstant() ) ;
        totalDurationDays    = (int) duration.toDays() ;
        exerciseStartDate    = DateUtils.addDays( startDate, bufferLeft + theoryMargin ) ;
        exerciseEndDate      = DateUtils.addDays( endDate, -bufferRight ) ;
        exerciseDurationDays = totalDurationDays - bufferLeft - theoryMargin - bufferRight ;
        currentZone          = computeCurrentZone() ;
        originalBurnRate     = (int)Math.ceil((float)totalProblemsCount/exerciseDurationDays) ;
        requiredBurnRate     = originalBurnRate ;
        
        if( currentZone == Zone.EXERCISE ) {
            remainingExerciseDays = (int)Duration.between( new Date().toInstant(), exerciseEndDate.toInstant() ).toDays() ;
            if( remainingExerciseDays > 0 ) {
                requiredBurnRate = (int)Math.ceil((float)remainingProblemCount/remainingExerciseDays) ;
            }
            else {
                requiredBurnRate = remainingProblemCount ;
            }
        }
        
        initializeL30StudyTime();
    }
    
    private void initializeL30StudyTime() {
        Date today = DateUtils.truncate( new Date(), Calendar.DAY_OF_MONTH ) ;
        Date startDate = DateUtils.addDays( today, -29 ) ;
        
        l30StudyTime.clear() ;
        l30StudyTime.put( startDate, 0 ) ;
        for( int i=1; i<30; i++ ) {
            Date date = DateUtils.addDays( startDate, i ) ;
            l30StudyTime.put( date, 0 ) ;
        }
        
        studyTimeRepo.getStudyTimesFromDate( startDate, topic.getSyllabusName() )
                .forEach( studyTime -> {
                    l30StudyTime.put( studyTime.getId().getDate(),
                                      studyTime.getTotalTime().intValue() ) ;
                });
    }
    
    private Zone computeCurrentZone() {
        
        Date blStart = startDate ;
        Date blEnd   = DateUtils.addDays( blStart, bufferLeft ) ;
        Date thEnd   = DateUtils.addDays( blEnd, theoryMargin ) ;
        Date exStart = exerciseStartDate ;
        Date exEnd   = exerciseEndDate ;
        Date brStart = exerciseStartDate ;
        Date brEnd   = endDate ;
        
        Date today = new Date() ;
        if( today.before( startDate ) ) {
            return Zone.PRE_START ;
        }
        else if( isBetween( blStart, blEnd, today ) ) {
            return Zone.BUFFER_START ;
        }
        else if( isBetween( blEnd, thEnd, today ) ) {
            return Zone.THEORY ;
        }
        else if( isBetween( exStart, exEnd, today ) ) {
            return Zone.EXERCISE ;
        }
        else if( isBetween( brStart, brEnd, today ) ) {
            return Zone.BUFFER_END ;
        }
        return Zone.POST_END ;
    }
}
