package com.sandy.sconsole.state;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.dao.master.TopicTrackAssignment;
import com.sandy.sconsole.dao.master.dto.TopicVO;
import com.sandy.sconsole.dao.master.repo.TopicProblemRepo;
import com.sandy.sconsole.dao.master.repo.TopicRepo;
import com.sandy.sconsole.dao.session.dto.SessionDTO;
import com.sandy.sconsole.dao.session.repo.ProblemAttemptRepo;
import com.sandy.sconsole.dao.session.repo.TodaySolvedProblemCountRepo;
import com.sandy.sconsole.state.manager.ProblemStateCounter;
import com.sandy.sconsole.state.manager.TodaySessionStatistics;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.jfree.data.statistics.Regression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.sandy.sconsole.core.util.SConsoleUtil.*;

@Slf4j
@Component
@Scope( "prototype" )
public class ActiveTopicStatistics {
    
    public static final SimpleDateFormat DF = new SimpleDateFormat( "yyyy-MMM-dd" ) ;
    
    public enum Zone { PRE_START, COACHING, SELF_STUDY, EXERCISE, CONSOLIDATiON, POST_END }
    
    @Autowired private TopicRepo topicRepo ;
    @Autowired private TodaySolvedProblemCountRepo tspcRepo ;
    @Autowired private ProblemAttemptRepo paRepo ;
    @Autowired private TopicProblemRepo tpRepo;
    
    @Getter private TopicVO topic ;
    
    // Static information from the topic plan
    @Getter private int  topicId ;
    @Getter private Date startDate ;
    @Getter private int  coachingNumDays ;
    @Getter private int  selfStudyNumDays ;
    @Getter private int  consolidationNumDays ;
    @Getter private Date endDate ;
    @Getter private int  interTopicGapNumDays ;
    
    // Derived information from the topic plan
    @Getter private int  numTotalDays ;
    @Getter private int  numExerciseDays ;
    @Getter private Date coachingStartDate ;
    @Getter private Date coachingEndDate ;
    @Getter private Date selfStudyStartDate ;
    @Getter private Date selfStudyEndDate ;
    @Getter private Date exerciseStartDate ;
    @Getter private Date exerciseEndDate ;
    @Getter private Date consolidationStartDate ;
    @Getter private Date consolidationEndDate ;
    @Getter private int  numTotalProblems ;
    @Getter private int  originalBurnRate ;
    
    // Derived in relation to the current date
    @Getter private Zone currentZone ;
    @Getter private int numProblemsLeft ;
    @Getter private int numExerciseDaysLeft;
    @Getter private int numPigeonedProblems ;
    @Getter private int currentBurnRate ;
    
    // Today data
    @Getter private int numProblemsSolvedToday = 0 ;
    
    // Projection based on current data
    @Getter private int requiredBurnRate ;
    @Getter private int numOvershootDays ;
    
    @Getter private final ProblemStateCounter allProblemsStateCounter = new ProblemStateCounter() ;
    @Getter private final ProblemStateCounter todayProblemsStateCounter = new ProblemStateCounter() ;
    @Getter private final ProblemStateCounter currentSessionProblemStates = new ProblemStateCounter() ;
    
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
        this.numPigeonedProblems = 0 ;
        this.originalBurnRate = 0 ;
        this.currentBurnRate = 0 ;
        this.requiredBurnRate = 0 ;
        this.numOvershootDays = 0 ;
        this.numProblemsSolvedToday = 0 ;

        this.topicId              = assignment.getTopicId() ;
        this.startDate            = assignment.getStartDate() ;
        this.endDate              = assignment.getEndDate() ; // End time is 23:59:59 of the end date
        this.coachingNumDays      = assignment.getCoachingNumDays() ;
        this.selfStudyNumDays     = assignment.getSelfStudyNumDays() ;
        this.consolidationNumDays = assignment.getConsolidationNumDays() ;
        this.interTopicGapNumDays = assignment.getInterTopicGapNumDays() ;
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
        
        // Derived information from the topic plan
        numTotalDays      = durationDays( startDate, endDate ) ;
        numExerciseDays   = numTotalDays - coachingNumDays - selfStudyNumDays - consolidationNumDays ;
        
        coachingStartDate      = startDate ;
        coachingEndDate        = DateUtils.addDays( startDate, coachingNumDays-1 ) ;
        selfStudyStartDate     = DateUtils.addDays( startDate, coachingNumDays ) ;
        selfStudyEndDate       = DateUtils.addDays( startDate, coachingNumDays + selfStudyNumDays-1 ) ;
        exerciseStartDate      = DateUtils.addDays( startDate, coachingNumDays + selfStudyNumDays ) ;
        exerciseEndDate        = DateUtils.addDays( exerciseStartDate, numExerciseDays-1 ) ;
        consolidationStartDate = DateUtils.addDays( endDate, -consolidationNumDays ) ;
        consolidationEndDate   = endDate ;
        
        numTotalProblems  = topicRepo.getTotalProblemCount( topicId ) ;
        originalBurnRate  = Math.round( (float)numTotalProblems / numExerciseDays ) ;
        
        // Derived in relation to the current date
        currentZone         = computeCurrentZone() ;
        numProblemsLeft     = topicRepo.getRemainingProblemCount( topicId ) ;
        numExerciseDaysLeft = durationDays( new Date(), exerciseEndDate ) ;
        
        // Today data
        Integer numPigeons = tpRepo.findNumPigeonedProblems( topicId ) ;
        numPigeonedProblems = ( numPigeons == null ) ? 0 : numPigeons ;
        
        Integer tempInt = tspcRepo.getNumSolvedProblems( topicId ) ;
        numProblemsSolvedToday = tempInt == null ? 0 : tempInt ;
        
        computeRequiredBurnRate() ;
        computeCurrentBurnAndOvershoot() ;
        
        // Populate the problem state counters
        this.allProblemsStateCounter.populateCounts( tpRepo.getProblemStateCounts( topicId ) ) ;
        this.todayProblemsStateCounter.populateCounts( tpRepo.getProblemStateCountsForToday( topicId ) ) ;
        updateCurrentSessionProblemStates() ;
        
//        log.debug( "       Start date          - {}", DF.format( startDate ) ) ;
//        log.debug( "       Coaching start      - {}", DF.format( coachingStartDate ) ) ;
//        log.debug( "       Coaching end        - {}", DF.format( coachingEndDate ) ) ;
//        log.debug( "       Self study start    - {}", DF.format( selfStudyStartDate ) ) ;
//        log.debug( "       Self study end      - {}", DF.format( selfStudyEndDate ) ) ;
//        log.debug( "       Exercise start      - {}", DF.format( exerciseStartDate ) ) ;
//        log.debug( "       Exercise end        - {}", DF.format( exerciseEndDate ) ) ;
//        log.debug( "       Consolidation start - {}", DF.format( consolidationStartDate ) ) ;
//        log.debug( "       Consolidation end   - {}", DF.format( consolidationEndDate ) ) ;
//        log.debug( "       End date            - {}", DF.format( endDate ) ) ;
//        log.debug( "       Total days          - {}", numTotalDays ) ;
//        log.debug( "       Exercise days       - {}", numExerciseDays ) ;
//        log.debug( "       Ex days left        - {}", numExerciseDaysLeft ) ;
//        log.debug( "       Planned burn        - {}", originalBurnRate ) ;
    }
    
    public void updateCurrentSessionProblemStates() {
        TodaySessionStatistics todaySessionStatistics = SConsole.getBean( TodaySessionStatistics.class ) ;
        SessionDTO currentSession = todaySessionStatistics.getCurrentSession() ;
        if( currentSession != null ) {
            this.currentSessionProblemStates.populateCounts(
                    tpRepo.getProblemStateCountsForSession(
                            currentSession.getTopicId(), currentSession.getId()
                    )
            ) ;
        }
        else {
            this.currentSessionProblemStates.resetCountsToZero() ;
        }
    }
    
    private void computeRequiredBurnRate() {
        
        requiredBurnRate = 0 ;
        
        if( currentZone == Zone.PRE_START ||
            currentZone == Zone.COACHING ||
            currentZone == Zone.SELF_STUDY ) {

            // If we are before the exercise start date, the current burn rate is considered zero
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
    }
    
    private void computeCurrentBurnAndOvershoot() {
        
        this.currentBurnRate = 0 ;
        this.numOvershootDays = 0 ;
    
        List<ProblemAttemptRepo.DayBurn> dayBurns = paRepo.getHistoricBurns( topicId ) ;
        if( dayBurns.isEmpty() ) return ;
        
        double[][] data = new double[dayBurns.size()+2][2] ;
        int remainingProblems = numTotalProblems ;
        
        Date startDate = DateUtils.addDays( dayBurns.get( 0 ).getDate(), -1 ) ;
        data[0][0] = startDate.getTime() ;
        data[0][1] = remainingProblems ;
        
        for( int i=1; i<data.length-1; i++ ) {
            ProblemAttemptRepo.DayBurn db = dayBurns.get( i-1 ) ;
            remainingProblems -= db.getNumQuestionsSolved() ;
            
            data[i][0] = db.getDate().getTime() ;
            data[i][1] = remainingProblems ;
        }
        
        data[data.length-1][0] = new Date().getTime() ;
        data[data.length-1][1] = remainingProblems ;
        
        double[] coefficients = Regression.getOLSRegression( data ) ;
        
        long xIntercept = (long)(-coefficients[0]/coefficients[1]) ;
        Date projectedCompletionDate = new Date( xIntercept ) ;
        
        this.numOvershootDays = durationDays( exerciseEndDate, projectedCompletionDate ) ;
        this.currentBurnRate = (int)Math.round( -coefficients[1]*86400*1000 ) ;
    }
    
    private Zone computeCurrentZone() {
        
        Date today = new Date() ;
        if( today.before( startDate ) ) {
            return Zone.PRE_START ;
        }
        else if( isBetween( coachingStartDate, coachingEndDate, today ) ) {
            return Zone.COACHING ;
        }
        else if( isBetween( coachingEndDate, selfStudyEndDate, today ) ) {
            return Zone.SELF_STUDY ;
        }
        else if( isBetween( exerciseStartDate, exerciseEndDate, today ) ) {
            return Zone.EXERCISE ;
        }
        else if( isBetween( consolidationStartDate, consolidationEndDate, today ) ) {
            return Zone.CONSOLIDATiON ;
        }
        return Zone.POST_END ;
    }
    
    public List<ProblemAttemptRepo.DayBurn> getHistoricBurns() {
        return paRepo.getHistoricBurns( topicId ) ;
    }
    
    public boolean isCurrentlyActive() {
        Date today = new Date() ;
        return today.after( startDate ) && today.before( nextDay( endDate ) ) ;
    }
}
