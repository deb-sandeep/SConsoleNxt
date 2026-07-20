package com.sandy.sconsole.endpoints.websockets.monitor.payload;

import com.sandy.sconsole.core.util.ColorUtil;
import com.sandy.sconsole.state.ActiveTopicStatistics;
import com.sandy.sconsole.state.manager.ProblemStateCounter;
import lombok.Getter;

@Getter
class ProblemStateBreakdown {

    private final int totalCount ;
    private final int numAssigned ;
    private final int numCorrect ;
    private final int numIncorrect ;
    private final int numLater ;
    private final int numPigeons ;
    private final int numPigeonsExplained ;
    private final int numPigeonsSolved ;
    private final int numPurged ;
    private final int numReassign ;
    private final int numRedo ;

    ProblemStateBreakdown( ProblemStateCounter c ) {
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

/**
 * Lightweight, frequently-refreshed subset of a topic's live stats — pushed over the
 * app-monitor websocket whenever ActiveTopicStatistics.refreshState() runs for this topic.
 * Slower-changing plan/chart data (dates, historic burn series) is served separately over
 * REST by ActiveTopicChartAPI, and is not duplicated here.
 */
@Getter
public class TopicDetailState {

    private final int topicId ;
    private final int numProblemsLeft ;
    private final int currentBurnRate ;
    private final int requiredBurnRate ;
    private final int numOvershootDays ;
    private final double burnStressScore ;
    private final String burnStressZone ;
    private final String burnStressZoneColor ;
    private final int numProblemsSolvedToday ;
    private final boolean burnMetOverride ;
    private final ProblemStateBreakdown allTimeProblemState ;
    private final ProblemStateBreakdown todayProblemState ;

    public TopicDetailState( ActiveTopicStatistics ts ) {
        this.topicId = ts.getTopicId() ;
        this.numProblemsLeft = ts.getNumProblemsLeft() ;
        this.currentBurnRate = ts.getCurrentBurnRate() ;
        this.requiredBurnRate = ts.getRequiredBurnRate() ;
        this.numOvershootDays = ts.getNumOvershootDays() ;
        this.burnStressScore = ts.getBurnStressScore() ;
        this.burnStressZone = ts.getBurnStressScoreLabel() ;
        this.burnStressZoneColor = ColorUtil.toHtmlColor( ts.getBurnStressScoreColor() ) ;
        this.numProblemsSolvedToday = ts.getNumProblemsSolvedToday() ;
        this.burnMetOverride = ts.isBurnMetOverride() ;
        this.allTimeProblemState = new ProblemStateBreakdown( ts.getAllProblemsStateCounter() ) ;
        this.todayProblemState = new ProblemStateBreakdown( ts.getTodayProblemsStateCounter() ) ;
    }
}
