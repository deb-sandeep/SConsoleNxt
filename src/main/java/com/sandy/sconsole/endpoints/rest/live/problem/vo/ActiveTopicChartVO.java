package com.sandy.sconsole.endpoints.rest.live.problem.vo ;

import com.fasterxml.jackson.annotation.JsonFormat ;
import com.sandy.sconsole.state.manager.ProblemStateCounter;
import lombok.Data ;

import java.util.Date ;
import java.util.List ;

@Data
public class ActiveTopicChartVO {

    @Data
    public static class TopicInfo {
        private int    topicId ;
        private String syllabusName ;
        private String sectionName ;
        private String topicName ;
    }

    @Data
    public static class PlanMetrics {
        @JsonFormat( shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "IST" )
        private Date startDate ;
        @JsonFormat( shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "IST" )
        private Date coachingEndDate ;      // null when coachingNumDays == 0
        @JsonFormat( shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "IST" )
        private Date selfStudyEndDate ;     // null when selfStudyNumDays == 0
        @JsonFormat( shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "IST" )
        private Date exerciseStartDate ;
        @JsonFormat( shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "IST" )
        private Date exerciseEndDate ;
        @JsonFormat( shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "IST" )
        private Date endDate ;
        private int numTotalProblems ;
        private int numExerciseDays ;
        private int originalBurnRate ;      // planned problems/day at assignment time
    }

    @Data
    public static class StatusMetrics {
        private String currentZone ;        // name() of ActiveTopicStatistics.Zone enum
        private int    numProblemsLeft ;
        private int    currentBurnRate ;    // problems/day (OLS over last 8 days)
        private int    requiredBurnRate ;   // problems/day needed to finish on time
        private double burnStressScore ;    // tanh-normalised in (-1, +1); negative = ahead
        private String scoreLabel ;         // e.g. "SLIGHT LAG", "ROCKIN!!"
        private int    numOvershootDays ;   // projected days past exerciseEndDate
        private int    leadLagProblems ;    // actual minus ideal remaining (positive = lagging)
        private int    numExerciseDaysLeft ;
    }

    @Data
    public static class BurnPoint {
        @JsonFormat( shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "IST" )
        private Date date ;
        private int  remaining ;

        public BurnPoint( Date date, int remaining ) {
            this.date      = date ;
            this.remaining = remaining ;
        }
    }

    @Data
    public static class DayCountPoint {
        @JsonFormat( shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "IST" )
        private Date date ;
        private int  numSolved ;

        public DayCountPoint( Date date, int numSolved ) {
            this.date      = date ;
            this.numSolved = numSolved ;
        }
    }

    @Data
    public static class ProblemStateBreakdown {
        private int totalCount ;
        private int numAssigned ;
        private int numCorrect ;
        private int numIncorrect ;
        private int numLater ;
        private int numPigeons ;
        private int numPigeonsExplained ;
        private int numPigeonsSolved ;
        private int numPurged ;
        private int numReassign ;
        private int numRedo ;

        public ProblemStateBreakdown( ProblemStateCounter c ) {
            this.totalCount          = c.getTotalCount() ;
            this.numAssigned         = c.getNumAssigned() ;
            this.numCorrect          = c.getNumCorrect() ;
            this.numIncorrect        = c.getNumIncorrect() ;
            this.numLater            = c.getNumLater() ;
            this.numPigeons          = c.getNumPigeons() ;
            this.numPigeonsExplained = c.getNumPigeonsExplained() ;
            this.numPigeonsSolved    = c.getNumPigeonsSolved() ;
            this.numPurged           = c.getNumPurged() ;
            this.numReassign         = c.getNumReassign() ;
            this.numRedo             = c.getNumRedo() ;
        }
    }

    private TopicInfo       topic ;
    private PlanMetrics     plan ;
    private StatusMetrics   status ;
    private List<BurnPoint> actualBurn ;    // historic solved + today's position
    private List<BurnPoint> idealBurn ;     // 2-4 inflection points for Chart.js line
    private List<BurnPoint> projectedBurn ; // forward from last actual at currentBurnRate

    private String burnStressZoneColor ;    // HTML hex color for status.burnStressScore
    private int numPigeonedProblems ;
    private int numProblemsSolvedToday ;
    private ProblemStateBreakdown allTimeProblemState ;
    private ProblemStateBreakdown todayProblemState ;
    private List<DayCountPoint> l30Burn ;    // last 30 days, problems solved per day
}
