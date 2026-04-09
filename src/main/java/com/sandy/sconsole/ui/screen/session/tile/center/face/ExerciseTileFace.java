package com.sandy.sconsole.ui.screen.session.tile.center.face;

import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.dao.master.Chapter;
import com.sandy.sconsole.dao.master.Problem;
import com.sandy.sconsole.dao.master.repo.ProblemRepo;
import com.sandy.sconsole.dao.session.dto.ProblemAttemptDTO;
import com.sandy.sconsole.dao.session.repo.ProblemAttemptRepo;
import com.sandy.sconsole.endpoints.rest.live.session.vo.SessionExtensionVO;
import com.sandy.sconsole.state.ActiveTopicStatistics;
import com.sandy.sconsole.state.manager.ActiveTopicStatisticsManager;
import com.sandy.sconsole.state.manager.TodaySessionStatistics;
import com.sandy.sconsole.ui.screen.session.tile.ProblemStateCounterRowPanel;
import info.clearthought.layout.TableLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

import static com.sandy.sconsole.EventCatalog.*;
import static com.sandy.sconsole.core.ui.uiutil.UITheme.BG_COLOR;
import static com.sandy.sconsole.ui.screen.session.tile.ProblemStateCounterTile.COLUMN_VALUE_COLORS;
import static com.sandy.sconsole.ui.screen.session.tile.ProblemStateCounterTile.LABEL_FG_COLOR;
import static javax.swing.SwingConstants.CENTER;

@Component
@Scope( "prototype" )
public class ExerciseTileFace extends Tile
    implements EventSubscriber {

    private static final Font BOOK_FONT            = new Font( Font.MONOSPACED, Font.PLAIN, 33 ) ;
    private static final Font CHAPTER_FONT         = new Font( Font.MONOSPACED, Font.PLAIN, 20 ) ;
    private static final Font PROBLEM_FONT         = new Font( Font.MONOSPACED, Font.PLAIN, 26 ) ;
    private static final Font STATE_BADGE_FONT     = new Font( Font.MONOSPACED, Font.PLAIN, 26 ) ;
    private static final Font PROBLEM_TIMER_FONT   = new Font( Font.MONOSPACED, Font.PLAIN, 35 ) ;
    private static final Font PROBLEM_INSIGHT_FONT = new Font( Font.MONOSPACED, Font.PLAIN, 35 ) ;
    
    private static final Color BOOK_NAME_COLOR           = new Color( 33, 134, 159 ) ;
    private static final Color CHP_NAME_COLOR            = new Color( 163, 113, 255 ) ;
    private static final Color PROBLEM_KEY_COLOR         = new Color( 174, 86, 0 ) ;
    private static final Color PROBLEM_TIMER_FG_COLOR    = new Color( 101, 101, 101 ) ;
    private static final Color TOTAL_TIME_SPENT_FG_COLOR = new Color( 2, 132, 166 ) ;

    @Autowired private EventBus eventBus ;
    @Autowired private ProblemRepo problemRepo ;
    @Autowired private ProblemAttemptRepo problemAttemptRepo ;
    @Autowired private TodaySessionStatistics todaySessionStats ;
    @Autowired private ActiveTopicStatisticsManager atsManager ;

    private ActiveTopicStatistics ats = null ;
    private CurrentProblemContext currentProblemContext = null ;
    
    private final ProblemStateCounterRowPanel sessionCountRowPanel =
            new ProblemStateCounterRowPanel( "Session", 0 ) ;

    private final JLabel bookNameLabel = new JLabel() ;
    private final JLabel chapterNameLabel = new JLabel() ;
    private final JLabel problemKeyLabel = new JLabel() ;
    private final JLabel currentStateBadgeLabel = new JLabel() ;
    private final JLabel currentProblemTimerLabel = new JLabel() ;
    private final JLabel totalTimeSpentLabel = new JLabel() ;

    public ExerciseTileFace() {
        setUpUI() ;
    }

    private void setUpUI() {

        setBackground( BG_COLOR ) ;

        TableLayout layout = new TableLayout() ;
        layout.insertColumn( 0, TableLayout.FILL ) ;
        layout.insertRow( 0, 60 ) ; // Section problem count row
        layout.insertRow( 1, 60 ) ; // Book name row
        layout.insertRow( 2, 50 ) ; // Chapter name row
        layout.insertRow( 3, 65 ) ; // Problem key row
        setLayout( layout ) ;
        
        add( sessionCountRowPanel, "0,0" ) ;
        add( configureLabel( bookNameLabel, BOOK_FONT, BOOK_NAME_COLOR ), "0,1" ) ;
        add( createChapterDetailPanel(), "0,2" ) ;
        add( createCurrentProblemInsightPanel(), "0,3" ) ;
    }

    private JPanel createChapterDetailPanel() {

        JPanel panel = new JPanel( new FlowLayout( FlowLayout.CENTER, 10, 10 ) ) ;
        panel.setBackground( BG_COLOR ) ;

        panel.add( configureLabel( chapterNameLabel, CHAPTER_FONT, CHP_NAME_COLOR ) ) ;
        panel.add( configureLabel( problemKeyLabel, PROBLEM_FONT, PROBLEM_KEY_COLOR ) ) ;
        return panel ;
    }

    private JPanel createCurrentProblemInsightPanel() {

        JPanel panel = new JPanel() ;
        panel.setBackground( BG_COLOR ) ;

        TableLayout layout = new TableLayout() ;
        layout.insertRow( 0, TableLayout.FILL ) ;
        layout.insertColumn( 0, 0.33 ) ;
        layout.insertColumn( 1, 0.33 ) ;
        layout.insertColumn( 2, 0.33 ) ;
        panel.setLayout( layout ) ;

        panel.add( configureLabel( currentStateBadgeLabel,
                                   STATE_BADGE_FONT,
                                   LABEL_FG_COLOR ),
                   "0,0" ) ;
        panel.add( configureLabel( currentProblemTimerLabel,
                                   PROBLEM_TIMER_FONT,
                                   PROBLEM_TIMER_FG_COLOR ),
                   "1,0" ) ;
        panel.add( configureLabel( totalTimeSpentLabel,
                                   PROBLEM_INSIGHT_FONT,
                                   TOTAL_TIME_SPENT_FG_COLOR ),
                   "2,0" ) ;
        return panel ;
    }

    private JLabel configureLabel( JLabel label, Font font, Color fgColor ) {

        label.setHorizontalAlignment( CENTER ) ;
        label.setVerticalAlignment( CENTER ) ;
        label.setOpaque( true ) ;
        label.setForeground( fgColor ) ;
        label.setBackground( BG_COLOR ) ;
        label.setFont( font ) ;
        return label ;
    }

    @Override
    public void beforeActivation() {

        this.ats = atsManager.getTopicStatistics( todaySessionStats.getCurrentSession().getTopicId() ) ;

        eventBus.addAsyncSubscriber( this, ATS_REFRESHED ) ;
        eventBus.addAsyncSubscriber( this, PROBLEM_ATTEMPT_ENDED ) ;
        eventBus.addAsyncSubscriber( this, PROBLEM_ATTEMPT_STARTED ) ;
        eventBus.addAsyncSubscriber( this, SESSION_EXTENDED ) ;

        refreshSessionCounts() ;
        clearProblemDetails() ;
    }

    @Override
    public void beforeDeactivation() {
        eventBus.removeSubscriber( this ) ;
        this.ats = null ;
        clearProblemDetails() ;
    }

    @Override
    public synchronized void handleEvent( Event event ) {

        switch( event.getEventId() ) {
            case ATS_REFRESHED -> handleATSRefreshed( event ) ;
            case PROBLEM_ATTEMPT_ENDED -> clearProblemDetails() ;
            case PROBLEM_ATTEMPT_STARTED -> handleProblemAttemptUpdated( ( ProblemAttemptDTO )event.getValue() ) ;
            case SESSION_EXTENDED -> handleProblemAttemptUpdated(
                    (( SessionExtensionVO )event.getValue()).getProblemAttemptDTO()
            ) ;
        }
    }

    private void handleATSRefreshed( Event event ) {

        if( ats == null || event.getValue() == null ) {
            return ;
        }

        Integer refreshedTopicId = ( Integer )event.getValue() ;
        if( refreshedTopicId == ats.getTopicId() ) {
            refreshSessionCounts() ;
        }
    }

    private void handleProblemAttemptUpdated( ProblemAttemptDTO attempt ) {

        if( attempt == null ) {
            return ;
        }

        if( currentProblemContext != null && currentProblemContext.isForAttempt( attempt ) ) {
            currentProblemContext = currentProblemContext.updateAttempt( attempt ) ;
        }
        else {
            Problem problem = problemRepo.findById( attempt.getProblemId() ).orElse( null ) ;
            currentProblemContext = createCurrentProblemContext( problem, attempt ) ;
        }

        refreshProblemDetails() ;
    }

    private void refreshSessionCounts() {

        SwingUtilities.invokeLater( () ->
                sessionCountRowPanel.setCounter(
                        ats == null ? null : ats.getCurrentSessionProblemStates() ) ) ;
    }

    private CurrentProblemContext createCurrentProblemContext( Problem problem,
                                                               ProblemAttemptDTO attempt ) {

        if( problem == null || attempt == null ) {
            return null ;
        }

        Integer numAttempts = problemAttemptRepo.getNumAttempts( problem.getId() ) ;
        Integer totalAttemptTime = problemAttemptRepo.getTotalAttemptTime( problem.getId() ) ;
        int currentAttemptTime = CurrentProblemContext.getEffectiveDuration( attempt ) ;
        int totalTimeBeforeCurrent =
                Math.max( 0, ( totalAttemptTime == null ? 0 : totalAttemptTime ) - currentAttemptTime ) ;

        return new CurrentProblemContext( problem,
                                          attempt,
                                          numAttempts == null ? 0 : numAttempts,
                                          totalTimeBeforeCurrent ) ;
    }

    private void refreshProblemDetails() {

        CurrentProblemContext context = currentProblemContext ;

        SwingUtilities.invokeLater( () -> {
            
            if( context == null ) {
                clearProblemDetailLabels() ;
                return ;
            }

            String bookShortName = context.problem.getChapter().getBook().getBookShortName() ;
            Chapter chapter = context.problem.getChapter() ;
            String chapterName = chapter.getId().getChapterNum() + ". " +
                                 chapter.getChapterName() ;
            String problemKey = "[" + context.problem.getProblemKey() + "]" ;
            String stateBadgeLabel = "[" + context.totalAttempts + "] " +
                                     context.attempt.getPrevState() ;
            
            bookNameLabel.setText( bookShortName ) ;
            chapterNameLabel.setText( chapterName ) ;
            problemKeyLabel.setText( problemKey ) ;
            currentStateBadgeLabel.setText( stateBadgeLabel ) ;
            
            currentStateBadgeLabel.setForeground(
                    getStateForegroundColor( context.attempt.getPrevState() ) ) ;
            
            currentProblemTimerLabel.setText(
                    getElapsedTimeLabelMMss( context.getCurrentAttemptTime() ) ) ;
            
            totalTimeSpentLabel.setText(
                    getElapsedTimeLabelMMss( context.getTotalAttemptTime() ) ) ;
        } ) ;
    }

    private void clearProblemDetails() {
        this.currentProblemContext = null ;
        refreshProblemDetails() ;
    }

    private void clearProblemDetailLabels() {
        bookNameLabel.setText( "" ) ;
        chapterNameLabel.setText( "" ) ;
        problemKeyLabel.setText( "" ) ;
        currentStateBadgeLabel.setText( "" ) ;
        currentProblemTimerLabel.setText( "" ) ;
        totalTimeSpentLabel.setText( "" ) ;
    }

    private String getElapsedTimeLabelMMss( int seconds ) {
        int minutes = seconds / 60 ;
        int secs = seconds % 60 ;
        return String.format( "%02d:%02d", minutes, secs ) ;
    }

    private Color getStateForegroundColor( String state ) {
        if( state == null ) {
            return LABEL_FG_COLOR ;
        }

        return switch( state ) {
            case "Correct" -> COLUMN_VALUE_COLORS[1] ;
            case "Incorrect" -> COLUMN_VALUE_COLORS[2] ;
            case "Later" -> COLUMN_VALUE_COLORS[3] ;
            case "Redo" -> COLUMN_VALUE_COLORS[4] ;
            case "Pigeon", "Pigeon Solved", "Pigeon Explained" -> COLUMN_VALUE_COLORS[5] ;
            case "Purge" -> COLUMN_VALUE_COLORS[6] ;
            case "Reassign" -> COLUMN_VALUE_COLORS[7] ;
            default -> LABEL_FG_COLOR ;
        } ;
    }
    
    private record CurrentProblemContext(
            Problem problem,
            ProblemAttemptDTO attempt,
            int totalAttempts,
            int totalAttemptTimeBeforeCurrent ) {
        
        private boolean isForAttempt( ProblemAttemptDTO attempt ) {
            return this.attempt != null &&
                   this.attempt.getId() != null &&
                   attempt != null && attempt.getId() != null &&
                   this.attempt.getId().equals( attempt.getId() );
        }
        
        private CurrentProblemContext updateAttempt( ProblemAttemptDTO attempt ) {
            return new CurrentProblemContext( problem, attempt, totalAttempts,
                                              totalAttemptTimeBeforeCurrent );
        }
        
        private int getCurrentAttemptTime() {
            return getEffectiveDuration( attempt );
        }
        
        private int getTotalAttemptTime() {
            return totalAttemptTimeBeforeCurrent + getCurrentAttemptTime();
        }
        
        private static int getEffectiveDuration( ProblemAttemptDTO attempt ) {
            return attempt.getEffectiveDuration() == null ?
                    0 : attempt.getEffectiveDuration();
        }
    }
}
