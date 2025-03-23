package com.sandy.sconsole.state;

import com.sandy.sconsole.dao.master.TopicTrackAssignment;
import com.sandy.sconsole.dao.master.dto.TopicVO;
import com.sandy.sconsole.dao.master.repo.TopicRepo;
import com.sandy.sconsole.dao.session.repo.TodaySolvedProblemCountRepo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.sandy.sconsole.core.util.SConsoleUtil.duration;
import static com.sandy.sconsole.core.util.SConsoleUtil.isBetween;

@Slf4j
@Component
@Scope( "prototype" )
public class ActiveTopicStatistics {
    
    public enum Zone { PRE_START, BUFFER_START, THEORY, EXERCISE, BUFFER_END, POST_END }
    
    @Autowired private TopicRepo topicRepo ;
    @Autowired private TodaySolvedProblemCountRepo tspcRepo ;
    
    @Getter private TopicVO topic ;
    
    // Static information from topic plan
    @Getter private int  topicId ;
    @Getter private int  numStartBufferDays;
    @Getter private int  numEndBufferDays;
    @Getter private int  numTheoryDays;
    @Getter private Date startDate ;
    @Getter private Date endDate ;
    
    // Derived information from topic plan
    @Getter private int  numTotalDays ;
    @Getter private int  numExerciseDays ;
    @Getter private Date exerciseStartDate ;
    @Getter private Date exerciseEndDate ;
    @Getter private int  numTotalProblems ;
    @Getter private int  originalBurnRate ;
    
    // Derived in relation to current date
    @Getter private Zone currentZone ;
    @Getter private int numProblemsLeft ;
    @Getter private int numExerciseDaysLeft;
    @Getter private int currentBurnRate ;
    
    // Today data
    @Getter private int numProblemsSolvedToday = 0 ;
    
    // Projection based on current data
    @Getter private int requiredBurnRate ;
    @Getter private int numOvershootDays ;
    
    public ActiveTopicStatistics() {}
    
    public void setTopicTrackAssignment( TopicTrackAssignment assignment ) {
        this.topic = null ;
        this.exerciseStartDate = null;
        this.exerciseEndDate = null;
        this.currentZone = null ;
        this.numTotalDays = 0 ;
        this.numExerciseDays = 0 ;
        this.numExerciseDaysLeft = 0 ;
        this.numTotalProblems = 0 ;
        this.numProblemsLeft = 0 ;
        this.originalBurnRate = 0 ;
        this.currentBurnRate = 0 ;
        this.requiredBurnRate = 0 ;
        this.numOvershootDays = 0 ;
        this.numProblemsSolvedToday = 0 ;

        this.topicId            = assignment.getTopicId() ;
        this.startDate          = assignment.getStartDate() ;
        this.endDate            = assignment.getEndDate() ;
        this.numStartBufferDays = assignment.getBufferLeft() ;
        this.numTheoryDays      = assignment.getTheoryMargin() ;
        this.numEndBufferDays   = assignment.getBufferRight() ;
    }
    
    public void init() {
        topic = new TopicVO( topicRepo.findById( topicId ).get() ) ;
        refreshState() ;
    }
    
    public void destroy() {}
    
    public int numProblemsCompleted() {
        return numTotalProblems - numProblemsLeft;
    }
    
    public void refreshState() {
        
        // Derived information from topic plan
        numTotalDays      = duration( startDate, endDate ) ;
        numExerciseDays   = numTotalDays - numStartBufferDays - numTheoryDays - numEndBufferDays ;
        exerciseStartDate = DateUtils.addDays( startDate, numStartBufferDays + numTheoryDays ) ;
        exerciseEndDate   = DateUtils.addDays( exerciseStartDate, numExerciseDays ) ;
        numTotalProblems  = topicRepo.getTotalProblemCount( topicId ) ;
        originalBurnRate  = Math.round( (float)numTotalProblems / numExerciseDays ) ;
        
        // Derived in relation to current date
        currentZone         = computeCurrentZone() ;
        numProblemsLeft     = topicRepo.getRemainingProblemCount( topicId ) ;
        numExerciseDaysLeft = duration( new Date(), exerciseEndDate ) ;
        currentBurnRate     = 0 ; // Will be calculated down the line
        
        // Today data
        Integer tempInt = tspcRepo.getNumSolvedProblems( topicId ) ;
        numProblemsSolvedToday = tempInt == null ? 0 : tempInt ;
        
        // Projection based on current data
        requiredBurnRate = 0 ;
        numOvershootDays = 0 ;
        
        if( currentZone == Zone.PRE_START ||
            currentZone == Zone.BUFFER_START ||
            currentZone == Zone.THEORY ) {
            // If we are before the exercise start date, current burn rate is considered zero
            // as there is nothing to track against.
            requiredBurnRate = (int)Math.ceil( (float)numProblemsLeft / numExerciseDaysLeft ) ;
        }
        else { // We are at or beyond the exercise start date
            int numDaysSinceExerciseStart = duration( exerciseEndDate, new Date() ) ;
            if( numProblemsLeft > 0 ) {
                currentBurnRate = Math.round((float)numProblemsCompleted()/numDaysSinceExerciseStart) ;
                if( currentZone == Zone.EXERCISE ) {
                    requiredBurnRate = (int)Math.ceil((float)numProblemsLeft / numExerciseDaysLeft ) ;
                }
                else {
                    requiredBurnRate = numProblemsLeft;
                }
                
                int numDaysToCompletionAtCurrentBurn = (int)Math.ceil( (float)numProblemsLeft / currentBurnRate ) ;
                numOvershootDays = numDaysToCompletionAtCurrentBurn - numExerciseDaysLeft;
            }
        }
    }
    
    private Zone computeCurrentZone() {
        
        Date blStart = startDate ;
        Date blEnd   = DateUtils.addDays( blStart, numStartBufferDays ) ;
        Date thEnd   = DateUtils.addDays( blEnd, numTheoryDays ) ;
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
