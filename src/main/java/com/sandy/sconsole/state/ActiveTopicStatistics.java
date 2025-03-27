package com.sandy.sconsole.state;

import com.sandy.sconsole.dao.master.TopicTrackAssignment;
import com.sandy.sconsole.dao.master.dto.TopicVO;
import com.sandy.sconsole.dao.master.repo.TopicRepo;
import com.sandy.sconsole.dao.session.repo.ProblemAttemptRepo;
import com.sandy.sconsole.dao.session.repo.TodaySolvedProblemCountRepo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.jfree.data.statistics.Regression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static com.sandy.sconsole.core.util.SConsoleUtil.duration;
import static com.sandy.sconsole.core.util.SConsoleUtil.isBetween;

@Slf4j
@Component
@Scope( "prototype" )
public class ActiveTopicStatistics {
    
    public enum Zone { PRE_START, BUFFER_START, THEORY, EXERCISE, BUFFER_END, POST_END }
    
    @Autowired private TopicRepo topicRepo ;
    @Autowired private TodaySolvedProblemCountRepo tspcRepo ;
    @Autowired private ProblemAttemptRepo paRepo ;
    
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
        this.endDate            = assignment.getEndDate() ; // End time is 23:59:59 of end date
        this.numStartBufferDays = assignment.getBufferLeft() ;
        this.numTheoryDays      = assignment.getTheoryMargin() ;
        this.numEndBufferDays   = assignment.getBufferRight() ;
    }
    
    public void init() {
        topic = new TopicVO( topicRepo.findById( topicId ).get() ) ;
        log.debug( "      Initializing active topic statistics for {}-{}",
                   topic.getSyllabusName(), topic.getTopicName() ) ;
        refreshState() ;
    }
    
    public void destroy() {}
    
    public int numProblemsCompleted() {
        return numTotalProblems - numProblemsLeft;
    }
    
    public void refreshState() {
        
        // Derived information from topic plan
        numTotalDays      = duration( startDate, endDate ) + 1 ;
        numExerciseDays   = numTotalDays - numStartBufferDays - numTheoryDays - numEndBufferDays ;
        exerciseStartDate = DateUtils.addDays( startDate, numStartBufferDays + numTheoryDays ) ;
        exerciseEndDate   = DateUtils.addDays( exerciseStartDate, numExerciseDays-1 ) ;
        numTotalProblems  = topicRepo.getTotalProblemCount( topicId ) ;
        originalBurnRate  = Math.round( (float)numTotalProblems / numExerciseDays ) ;
        
        // Derived in relation to current date
        currentZone         = computeCurrentZone() ;
        numProblemsLeft     = topicRepo.getRemainingProblemCount( topicId ) ;
        numExerciseDaysLeft = duration( new Date(), exerciseEndDate ) ;
        
        // Today data
        Integer tempInt = tspcRepo.getNumSolvedProblems( topicId ) ;
        numProblemsSolvedToday = tempInt == null ? 0 : tempInt ;
        
        // Projection based on current data
        requiredBurnRate = 0 ;
        
        if( currentZone == Zone.PRE_START ||
            currentZone == Zone.BUFFER_START ||
            currentZone == Zone.THEORY ) {
            // If we are before the exercise start date, current burn rate is considered zero
            // as there is nothing to track against.
            requiredBurnRate = (int)Math.ceil( (float)numProblemsLeft / numExerciseDaysLeft ) ;
        }
        else { // We are at or beyond the exercise start date
            if( numProblemsLeft > 0 ) {
                if( currentZone == Zone.EXERCISE ) {
                    requiredBurnRate = (int)Math.ceil((float)numProblemsLeft / numExerciseDaysLeft ) ;
                }
                else {
                    requiredBurnRate = numProblemsLeft;
                }
            }
        }
        computeCurrentBurnAndOvershoot() ;
    }
    
    private void computeCurrentBurnAndOvershoot() {
        
        this.currentBurnRate = 0 ;
        this.numOvershootDays = 0 ;
    
        List<ProblemAttemptRepo.DayBurnStat> dayBurns = paRepo.getHistoricBurnStats( topicId ) ;
        if( dayBurns.isEmpty() ) return ;
        
        double[][] data = new double[dayBurns.size()+1][2] ;
        int remainingProblems = numTotalProblems ;
        
        Date startDate = DateUtils.addDays( dayBurns.get( 0 ).getDate(), -1 ) ;
        data[0][0] = startDate.getTime() ;
        data[0][1] = remainingProblems ;
        
        for( int i=1; i<data.length; i++ ) {
            ProblemAttemptRepo.DayBurnStat db = dayBurns.get( i-1 ) ;
            remainingProblems -= db.getNumQuestionsSolved() ;
            
            data[i][0] = db.getDate().getTime() ;
            data[i][1] = remainingProblems ;
        }
        
        double[] coefficients = Regression.getOLSRegression( data ) ;
        
        long xIntercept = (long)(-coefficients[0]/coefficients[1]) ;
        Date projectedCompletionDate = new Date( xIntercept ) ;
        this.numOvershootDays = duration( endDate, projectedCompletionDate ) ;

        this.currentBurnRate = (int)Math.round( -coefficients[1]*86400*1000 ) ;
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
    
    public List<ProblemAttemptRepo.DayBurnStat> getHistoricBurns() {
        return paRepo.getHistoricBurnStats( topicId ) ;
    }
}
