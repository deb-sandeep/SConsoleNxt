package com.sandy.sconsole.state;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.dao.master.TopicTrackAssignment;
import com.sandy.sconsole.dao.master.repo.TopicProblemRepo;
import com.sandy.sconsole.dao.master.repo.TopicRepo;
import com.sandy.sconsole.dao.session.dto.SessionDTO;
import com.sandy.sconsole.dao.session.repo.ProblemAttemptRepo;
import com.sandy.sconsole.dao.session.repo.TodaySolvedProblemCountRepo;
import com.sandy.sconsole.endpoints.rest.master.core.vo.TopicVO;
import com.sandy.sconsole.state.manager.ProblemStateCounter;
import com.sandy.sconsole.state.manager.TodaySessionStatistics;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.jfree.data.statistics.Regression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.sandy.sconsole.core.util.SConsoleUtil.*;

@Slf4j
@Component
@Scope( "prototype" )
public class ActiveTopicStatistics {
    
    private static final boolean PRINT_TOPIC_DEBUG_INFO = false ;
    
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
    @Getter private int currentBurnRate ;
    @Getter private int leadLagProblems ;
    @Getter private double burnStressScore ;
    
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
        this.originalBurnRate = 0 ;
        this.currentBurnRate = 0 ;
        this.requiredBurnRate = 0 ;
        this.numOvershootDays = 0 ;
        this.leadLagProblems = 0 ;
        this.burnStressScore = 0.0 ;
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
        numTotalDays = durationDays( startDate, endDate ) ;
        numExerciseDays = numTotalDays - coachingNumDays - selfStudyNumDays - consolidationNumDays ;
        
        coachingStartDate = startDate ;
        if( coachingNumDays > 0 ) {
            coachingEndDate = DateUtils.addDays( startDate, coachingNumDays-1 ) ;
        }
        else {
            coachingEndDate = startDate ;
        }
        
        selfStudyStartDate = DateUtils.addDays( startDate, coachingNumDays ) ;
        if( selfStudyNumDays > 0 ) {
            selfStudyEndDate = DateUtils.addDays( startDate, coachingNumDays + selfStudyNumDays-1 ) ;
        }
        else {
            selfStudyEndDate = selfStudyStartDate ;
        }
        
        exerciseStartDate      = DateUtils.addDays( startDate, coachingNumDays + selfStudyNumDays ) ;
        exerciseEndDate        = DateUtils.addDays( exerciseStartDate, numExerciseDays-1 ) ;
        consolidationStartDate = DateUtils.addDays( endDate, -consolidationNumDays ) ;
        consolidationEndDate   = endDate ;
        
        numTotalProblems  = topicRepo.getTotalProblemCount( topicId ) ;
        originalBurnRate  = Math.round( (float)numTotalProblems / numExerciseDays ) ;
        
        // Derived in relation to the current date
        currentZone         = computeZoneAt( new Date() ) ;
        numProblemsLeft     = topicRepo.getRemainingProblemCount( topicId ) ;
        numExerciseDaysLeft = durationDays( new Date(), exerciseEndDate ) ;
        
        // Today data
        Integer tempInt = tspcRepo.getNumSolvedProblems( topicId ) ;
        numProblemsSolvedToday = tempInt == null ? 0 : tempInt ;
        
        computeRequiredBurnRate() ;
        computeCurrentBurnAndOvershoot() ;
        this.leadLagProblems = numProblemsLeft - getNumProblemsIdeallyRemainingAt( new Date() ) ;
        computeBurnStressScore() ;
        
        // Populate the problem state counters
        List<TopicProblemRepo.ProblemStateCount> problemStateCounts = tpRepo.getProblemStateCounts( topicId ) ;
        this.allProblemsStateCounter.populateCounts( problemStateCounts ) ;
        this.todayProblemsStateCounter.populateCounts( tpRepo.getProblemStateCountsForToday( topicId ) ) ;
        updateCurrentSessionProblemStates() ;
        
        if( PRINT_TOPIC_DEBUG_INFO ) {
            log.debug( "       Start date          - {}", DF.format( startDate ) ) ;
            log.debug( "       Coaching start      - {}", DF.format( coachingStartDate ) ) ;
            log.debug( "       Coaching end        - {}", DF.format( coachingEndDate ) ) ;
            log.debug( "       Self study start    - {}", DF.format( selfStudyStartDate ) ) ;
            log.debug( "       Self study end      - {}", DF.format( selfStudyEndDate ) ) ;
            log.debug( "       Exercise start      - {}", DF.format( exerciseStartDate ) ) ;
            log.debug( "       Exercise end        - {}", DF.format( exerciseEndDate ) ) ;
            log.debug( "       Consolidation start - {}", DF.format( consolidationStartDate ) ) ;
            log.debug( "       Consolidation end   - {}", DF.format( consolidationEndDate ) ) ;
            log.debug( "       End date            - {}", DF.format( endDate ) ) ;
            log.debug( "       Total days          - {}", numTotalDays ) ;
            log.debug( "       Exercise days       - {}", numExerciseDays ) ;
            log.debug( "       Ex days left        - {}", numExerciseDaysLeft ) ;
            log.debug( "       Planned burn        - {}", originalBurnRate ) ;
        }
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
            requiredBurnRate = (int)Math.ceil( (float)numProblemsLeft / numExerciseDaysLeft ) + 1 ;
        }
        else { // We are at or beyond the exercise start date
            if( numProblemsLeft > 0 ) {
                if( currentZone == Zone.EXERCISE ) {
                    requiredBurnRate = (int)Math.ceil((float)numProblemsLeft / numExerciseDaysLeft ) + 1 ;
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
        
        // If we are visiting this topic after its end date, there is no point
        // in computing the current burn and overshoot.
        if( currentZone == Zone.POST_END ) return ;
    
        List<ProblemAttemptRepo.DayBurn> dayBurns = paRepo.getHistoricBurns( topicId ) ;
        if( dayBurns.isEmpty() ) return ;
        
        // Why historic burn + 2 data points? We take the first point as the total
        // problems pegged to the day before the first burn and the last element
        // is the current date and the remaining problems
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
        
        // Instead of taking the full history for computing the current burn rate,
        // take the last one week burn. This forgives any past hiccups and focuses
        // on the current situation.
        final int NUM_PAST_DAYS = 8 ;
        double[][] recentBurnData ;
        if( data.length <= NUM_PAST_DAYS ) {
            recentBurnData = data ;
        }
        else {
            recentBurnData = new double[NUM_PAST_DAYS][2] ;
            System.arraycopy( data, data.length-NUM_PAST_DAYS, recentBurnData, 0, NUM_PAST_DAYS ) ;
        }
        
        double[] coefficients = Regression.getOLSRegression( recentBurnData ) ;
        
        long xIntercept = (long)(-coefficients[0]/coefficients[1]) ;
        Date projectedCompletionDate = new Date( xIntercept ) ;
        
        this.numOvershootDays = durationDays( exerciseEndDate, projectedCompletionDate ) ;
        this.currentBurnRate = (int)Math.round( -coefficients[1]*86400*1000 ) ;
    }

    /**
     * Computes burnStressScore — a normalized, bounded severity indicator in (−1, +1).
     *
     * Formula: tanh(2 × baseScore × accelerationMultiplier)
     *
     * baseScore = leadLagProblems / (numExerciseDaysLeft × originalBurnRate)
     *   — the lag expressed as a fraction of the remaining exercise capacity at the
     *     original planned rate. Equals the fractional increase in daily burn needed
     *     to recover: requiredBurnRate = originalBurnRate × (1 + baseScore).
     *
     * accelerationMultiplier [0.6, 1.5]
     *   — eases the score when recent burn is accelerating, heightens it when decelerating.
     *     Computed from two independent OLS windows over the last 14 days of history
     *     (separate from the 8-day velocity window used for currentBurnRate).
     *
     * tanh(2×) scaling: k=2 is calibrated to real-world experience where the required
     *   burn rarely exceeds 160% of planned (rawScore ≈ 0.6) before replanning, and 2×
     *   planned burn (rawScore = 1.0) maps to tanh(2.0) = 0.964 — "catastrophic".
     *   This places the full actionable range [0, 0.6] across ~83% of the display scale.
     *
     * Negative output = ahead of plan. Positive = behind.
     * Returns 0.0 if outside the exercise phase or burn history is unavailable.
     */
    private void computeBurnStressScore() {
        burnStressScore = 0.0 ;
        if( numExerciseDaysLeft <= 0 || originalBurnRate <= 0 ) return ;

        double baseScore = (double)leadLagProblems / ( (long)numExerciseDaysLeft * originalBurnRate ) ;

        List<ProblemAttemptRepo.DayBurn> dayBurns = paRepo.getHistoricBurns( topicId ) ;
        double multiplier = computeAccelerationMultiplier( dayBurns ) ;
        double rawScore   = baseScore * multiplier ;

        burnStressScore = Math.tanh( 2.0 * rawScore ) ;
    }

    /**
     * Returns an acceleration multiplier in [0.6, 1.5] based on whether the student's
     * burn rate has been speeding up or slowing down over the last 14 days.
     *
     * Uses two independent OLS windows over the 14-day history (completely separate from
     * the 8-day window used by computeCurrentBurnAndOvershoot for currentBurnRate):
     *   recent window : last 6 data points  → recentSlope (problems/day)
     *   prior  window : preceding data points → priorSlope  (problems/day)
     *
     * rawAcceleration = recentSlope − priorSlope
     *   Positive → burning faster lately (good) → multiplier < 1 → score eases
     *   Negative → burning slower lately (bad)  → multiplier > 1 → score heightens
     *
     * accelerationFactor = rawAcceleration / originalBurnRate normalises for topic size.
     * Clamped to [0.6, 1.5] so that even a sustained sprint or slump cannot swing the
     * final burnStressScore by more than ±40-50% of the base value.
     *
     * Returns 1.0 (neutral) when fewer than 6 days of history are available.
     */
    private double computeAccelerationMultiplier( List<ProblemAttemptRepo.DayBurn> dayBurns ) {
        final int ACCEL_WINDOW  = 14 ;
        final int RECENT_WINDOW =  6 ;

        if( dayBurns.size() < RECENT_WINDOW ) return 1.0 ;

        int available = Math.min( dayBurns.size(), ACCEL_WINDOW ) ;
        List<ProblemAttemptRepo.DayBurn> window =
                dayBurns.subList( dayBurns.size() - available, dayBurns.size() ) ;

        // Split: most recent RECENT_WINDOW entries vs the remainder as the prior window
        int recentCount = Math.min( RECENT_WINDOW, available / 2 ) ;
        int priorCount  = available - recentCount ;

        double recentSlope = olsBurnSlope( window.subList( available - recentCount, available ) ) ;
        double priorSlope  = olsBurnSlope( window.subList( 0, priorCount ) ) ;

        double rawAcceleration    = recentSlope - priorSlope ;
        double accelerationFactor = rawAcceleration / originalBurnRate ;

        // Multiplier < 1 eases stress (student is accelerating);
        // multiplier > 1 heightens stress (student is decelerating).
        return Math.max( 0.6, Math.min( 1.5, 1.0 - accelerationFactor ) ) ;
    }

    /**
     * Computes the OLS slope (problems solved per day) from a list of daily burn entries.
     * Fits a line to the cumulative problems-solved series using timestamps as X values.
     * Returns the slope in problems/day (positive = burning problems).
     * Returns 0.0 if fewer than 2 data points are provided.
     */
    private double olsBurnSlope( List<ProblemAttemptRepo.DayBurn> burns ) {
        if( burns.size() < 2 ) return 0.0 ;

        double[][] data = new double[burns.size()][2] ;
        double cumulative = 0 ;
        for( int i = 0; i < burns.size(); i++ ) {
            cumulative += burns.get( i ).getNumQuestionsSolved() ;
            data[i][0] = burns.get( i ).getDate().getTime() ;
            data[i][1] = cumulative ;
        }

        double[] coefficients = Regression.getOLSRegression( data ) ;
        // coefficients[1] is slope in problems/ms; convert to problems/day
        return coefficients[1] * 86_400_000 ;
    }
    
    /**
     * Returns the ideal number of problems that should remain at a given day offset
     * from the topic start date (0 = first day of topic).
     *
     * Zone logic uses pure integer arithmetic (not Date comparisons) for two reasons:
     *   1. The chart calls this for each plotted day using JFreeChart Day.getStart(),
     *      which returns exact midnight. isBetween() uses a strict date.after(start)
     *      check, so a midnight date equal to a zone boundary is not matched — causing
     *      the first day of each zone to be misclassified as POST_END (= 0), producing
     *      discontinuous drops in the chart.
     *   2. Integer day offsets are always unambiguous; Date comparisons near DST
     *      transitions can produce off-by-one errors.
     *
     * daysIntoExercise is 1-indexed (first exercise day = 1), matching the old chart
     * convention where the ideal burn begins on the first exercise day, not the second.
     *
     * The burn rate is kept as a double (numTotalProblems / numExerciseDays) to avoid
     * the integer-rounding error that would otherwise cause the line to hit zero before
     * the exercise end date (e.g. round(757/152) = 5, but 152*5 = 760 > 757).
     */
    public int getIdealRemainingAtDayOffset( int dayOffset ) {
        int exerciseStartDayNum = coachingNumDays + selfStudyNumDays ;
        int exerciseEndDayNum   = exerciseStartDayNum + numExerciseDays ;

        // No burn expected before exercise phase begins
        if( dayOffset < exerciseStartDayNum ) {
            return numTotalProblems ;
        }
        // Exercise window is closed; all problems should have been solved
        else if( dayOffset >= exerciseEndDayNum ) {
            return 0 ;
        }
        else {
            // +1 because exercise days are 1-indexed: on offset=exerciseStartDayNum
            // one full day's worth of burn is already expected.
            int daysIntoExercise = dayOffset - exerciseStartDayNum + 1 ;

            // Use a fractional rate — NOT the pre-rounded originalBurnRate integer —
            // so that accumulated rounding error does not shorten the ideal burn line.
            double idealBurnRate = (double)numTotalProblems / numExerciseDays ;
            return (int)Math.max( 0, Math.round( numTotalProblems - daysIntoExercise * idealBurnRate ) ) ;
        }
    }

    /**
     * Returns the ideal number of problems that should remain at an arbitrary Date,
     * using zone detection via Date comparisons.
     *
     * Safe to call with new Date() (current wall-clock time) because isBetween() uses
     * a strict date.after(start) check — and the current time (e.g. 10:00 AM) is always
     * strictly after the zone boundary midnight dates stored in the DB.  Do NOT call
     * this with Day.getStart() or any other midnight timestamp; use
     * getIdealRemainingAtDayOffset() instead (see its Javadoc for the reason).
     *
     * Used to compute leadLagProblems: how far ahead or behind the ideal plan the
     * student currently is.  Positive result = lagging; negative = ahead.
     *
     * The burn rate is kept as a double for the same float-precision reason described
     * in getIdealRemainingAtDayOffset.
     */
    public int getNumProblemsIdeallyRemainingAt( Date date ) {
        Zone zone = computeZoneAt( date ) ;
        return switch( zone ) {
            // Student is not expected to solve problems outside the exercise phase
            case PRE_START, COACHING, SELF_STUDY -> numTotalProblems ;
            case EXERCISE -> {
                // durationDays is inclusive on both ends (same-day = 1), so
                // daysIntoExercise is 1 on the first exercise day — matching the
                // 1-indexed convention in getIdealRemainingAtDayOffset.
                int daysIntoExercise = durationDays( exerciseStartDate, date ) ;
                double idealBurnRate = (double)numTotalProblems / numExerciseDays ;
                yield (int)Math.max( 0, Math.round( numTotalProblems - daysIntoExercise * idealBurnRate ) ) ;
            }
            // Exercise window is closed; everything should have been solved
            case CONSOLIDATiON, POST_END -> 0 ;
        } ;
    }

    /**
     * Classifies the given date into a topic Zone (PRE_START → COACHING → SELF_STUDY →
     * EXERCISE → CONSOLIDATiON → POST_END) based on the date boundaries computed in
     * refreshState().
     *
     * isBetween() is strictly exclusive on the start boundary (date.after(start)), so
     * this method is reliable only when 'date' has a time component that is genuinely
     * after midnight — i.e. a real wall-clock timestamp such as new Date().  Passing a
     * midnight Date (e.g. Day.getStart()) risks misclassifying boundary days.
     */
    private Zone computeZoneAt( Date date ) {
        if( date.before( startDate ) ) {
            return Zone.PRE_START ;
        }
        else if( isBetween( coachingStartDate, coachingEndDate, date ) ) {
            return Zone.COACHING ;
        }
        else if( isBetween( selfStudyStartDate, selfStudyEndDate, date ) ) {
            return Zone.SELF_STUDY ;
        }
        else if( isBetween( exerciseStartDate, exerciseEndDate, date ) ) {
            return Zone.EXERCISE ;
        }
        else if( isBetween( consolidationStartDate, consolidationEndDate, date ) ) {
            return Zone.CONSOLIDATiON ;
        }
        return Zone.POST_END ;
    }
    
    /**
     * Zone boundary table — one entry per boundary, so length = number of zones + 1.
     * ZONE_LABELS[i] is the label for scores in [ZONE_BOUNDS[i], ZONE_BOUNDS[i+1]).
     * The AHEAD zone uses an inclusive upper bound (<=0.00); zoneIndexFor() handles this.
     */
    public static final double[] ZONE_BOUNDS = {
        -1.0, -0.40, -0.30, -0.20, 0.00, 0.20, 0.38, 0.66, 0.84, 0.96, 1.0
    } ;
    public static final String[] ZONE_LABELS = {
        "WAY AHEAD!!", "ROCKIN!!", "SLAYIN!", "AHEAD",
        "SLIGHT LAG", "MODERATE LAG", "CRITICAL LAG", "COOKED", "REPLAN !!", "CATASTROPHE"
    } ;

    /**
     * Returns the ZONE_LABELS index for a given score.
     * Threshold conditions exactly mirror those in getScoreLabel() — keep in sync.
     */
    public static int zoneIndexFor( double score ) {
        if( score < -0.40 ) return 0 ;  // WAY AHEAD!!
        if( score < -0.30 ) return 1 ;  // ROCKIN!!
        if( score < -0.20 ) return 2 ;  // SLAYIN!
        if( score <= 0.00 ) return 3 ;  // AHEAD      (note: <= for score==0.0 edge case)
        if( score <  0.20 ) return 4 ;  // SLIGHT LAG
        if( score <  0.38 ) return 5 ;  // MODERATE LAG
        if( score <  0.66 ) return 6 ;  // CRITICAL LAG
        if( score <  0.84 ) return 7 ;  // COOKED
        if( score <  0.96 ) return 8 ;  // REPLAN !!
        return 9 ;                       // CATASTROPHE
    }

    /** Returns the zone label for the current burnStressScore. */
    public String getBurnStressScoreLabel() {
        return ZONE_LABELS[ zoneIndexFor( burnStressScore ) ] ;
    }

    /**
     * Returns a colour for the current burnStressScore, interpolated from green (score=−1)
     * through yellow (score=0) to red (score=+1).
     *
     * Negative scores use pure green at varying brightness — brightest when furthest ahead,
     * to create a positive-reinforcement gradient (GOATED glows more than VIBIN).
     *
     * Positive scores rotate hue from yellow (score=0, hue=0.165) to red (score=+1, hue=0).
     */
    public Color getBurnStressScoreColor() {
        return burnStressScoreColor( burnStressScore ) ;
    }

    /**
     * Static overload — returns the interpolated color for any arbitrary score value.
     * Used by BurnHealthZoneBar to compute gradient colors at zone boundaries.
     */
    public static Color burnStressScoreColor( double score ) {
        if( score <= 0 ) {
            float brightness = 0.55f + 0.35f * (float)( -score ) ;
            return Color.getHSBColor( 0.33f, 1.0f, brightness ) ;
        }
        else {
            float hue = 0.165f * (float)( 1.0 - score ) ;
            return Color.getHSBColor( Math.max( 0f, hue ), 1.0f, 0.9f ) ;
        }
    }

    public List<ProblemAttemptRepo.DayBurn> getHistoricBurns() {
        return paRepo.getHistoricBurns( topicId ) ;
    }
    
    public boolean isCurrentlyActive() {
        Date today = new Date() ;
        return today.after( startDate ) && today.before( nextDay( endDate ) ) ;
    }
    
    public int getNumPigeonedProblems() {
        return this.allProblemsStateCounter.getNumPigeons() +
                this.allProblemsStateCounter.getNumPigeonsSolved() ;
    }
}
