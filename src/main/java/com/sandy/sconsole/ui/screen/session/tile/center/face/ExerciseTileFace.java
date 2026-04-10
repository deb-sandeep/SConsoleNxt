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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
@Scope( "prototype" )
public class ExerciseTileFace extends Tile
    implements EventSubscriber {
    
    private static class CurrentProblemContext {
        
        private final int initialTotalDuration ;
        
        @Getter private int currentDuration ;
        @Getter private int totalDuration ;
        
        private boolean paused = false ;
        
        CurrentProblemContext( int totalDuration ) {
            this.currentDuration = 0 ;
            this.initialTotalDuration = totalDuration ;
            this.totalDuration = totalDuration ;
        }
        
        public void setCurrentDuration( int currentDuration ) {
            this.currentDuration = currentDuration ;
            this.totalDuration = this.initialTotalDuration + currentDuration ;
        }
        
        public void incrementCurrentDuration() {
            setCurrentDuration( currentDuration + 1 ) ;
        }
    }

    private static final int TIMER_REFRESH_INTERVAL_MS = 1000 ;

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
    
    private final ProblemStateCounterRowPanel sessionCountRowPanel =
            new ProblemStateCounterRowPanel( "Session", 0 ) ;

    private final JLabel bookNameLabel = new JLabel() ;
    private final JLabel chapterNameLabel = new JLabel() ;
    private final JLabel problemKeyLabel = new JLabel() ;
    private final JLabel currentStateBadgeLabel = new JLabel() ;
    private final JLabel currentProblemTimerLabel = new JLabel() ;
    private final JLabel totalTimeSpentLabel = new JLabel() ;
    
    private CurrentProblemContext currentProblemContext = null ;
    
    public ExerciseTileFace() {
        setUpUI() ;
        
        Thread thread = new Thread( this::runTimerLoop, "TimeSpentUpdateDaemon" ) ;
        thread.setDaemon( true ) ;
        thread.start() ;
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

        JPanel panel = new JPanel( new FlowLayout( FlowLayout.CENTER, 10, 0 ) ) ;
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
    
    private void runTimerLoop() {
        
        while( !Thread.currentThread().isInterrupted() ) {
            try {
                Thread.sleep( TIMER_REFRESH_INTERVAL_MS ) ;
                if( currentProblemContext != null && !currentProblemContext.paused ) {
                    currentProblemContext.incrementCurrentDuration() ;
                    refreshProblemTimerLabels() ;
                }
            }
            catch( InterruptedException e ) {
                Thread.currentThread().interrupt() ;
                return ;
            }
        }
    }
    
    @Override
    public void beforeActivation() {

        this.ats = atsManager.getTopicStatistics( todaySessionStats.getCurrentSession().getTopicId() ) ;

        eventBus.addAsyncSubscriber( this, ATS_REFRESHED ) ;
        eventBus.addAsyncSubscriber( this, PROBLEM_ATTEMPT_ENDED ) ;
        eventBus.addAsyncSubscriber( this, PROBLEM_ATTEMPT_STARTED ) ;
        eventBus.addAsyncSubscriber( this, SESSION_EXTENDED ) ;
        eventBus.addAsyncSubscriber( this, PAUSE_STARTED ) ;
        eventBus.addAsyncSubscriber( this, PAUSE_ENDED ) ;

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
            case ATS_REFRESHED ->
                    refreshSessionCounts() ;
            
            case PROBLEM_ATTEMPT_STARTED -> {
                ProblemAttemptDTO attempt = ( ProblemAttemptDTO )event.getValue() ;
                displayProblemDetails( attempt ) ;
            }
            
            case PROBLEM_ATTEMPT_ENDED ->
                    clearProblemDetails() ;
            
            case SESSION_EXTENDED -> {
                SessionExtensionVO extension = ( SessionExtensionVO )event.getValue() ;
                if( extension.getProblemAttemptDTO() != null ) {
                    ProblemAttemptDTO attempt = extension.getProblemAttemptDTO() ;
                    currentProblemContext.setCurrentDuration( attempt.getEffectiveDuration() ) ;
                }
            }
            
            case PAUSE_STARTED -> {
                if( currentProblemContext != null ) {
                    currentProblemContext.paused = true ;
                }
            }
            
            case PAUSE_ENDED -> {
                if( currentProblemContext != null ) {
                    currentProblemContext.paused = false ;
                }
            }
        }
    }
    
    private void refreshSessionCounts() {
        SwingUtilities.invokeLater( () ->
                sessionCountRowPanel.setCounter(
                        ats == null ? null : ats.getCurrentSessionProblemStates() ) ) ;
    }
    
    private void displayProblemDetails( ProblemAttemptDTO attempt ) {

        SwingUtilities.invokeLater( () -> {
            
            int problemId = attempt.getProblemId() ;
            Problem problem = problemRepo.findById( problemId ).get() ;
            Chapter chapter = problem.getChapter() ;
            Integer numAttempts = problemAttemptRepo.getNumAttempts( problem.getId() ) ;
            
            String chapterName = chapter.getId().getChapterNum() + ". " + chapter.getChapterName() ;
            String prevState = attempt.getPrevState() ;
            int totalTime = problemAttemptRepo.getTotalAttemptTime( problem.getId() ) ;
            
            String bookShortName = problem.getChapter().getBook().getBookShortName() ;
            String problemKey = "[" + problem.getProblemKey() + "]" ;
            String stateBadgeLabel = "[" + numAttempts + "] " + prevState ;
            
            bookNameLabel.setText( bookShortName ) ;
            chapterNameLabel.setText( chapterName ) ;
            problemKeyLabel.setText( problemKey ) ;
            currentStateBadgeLabel.setText( stateBadgeLabel ) ;
            currentStateBadgeLabel.setForeground( getStateFgColor( prevState ) ) ;

            currentProblemContext = new CurrentProblemContext( totalTime ) ;
            refreshProblemTimerLabels() ;
        } ) ;
    }

    private void refreshProblemTimerLabels() {
        if( currentProblemContext != null ) {
            currentProblemTimerLabel.setText(
                    getElapsedTimeLabelMMss( currentProblemContext.currentDuration ) ) ;
    
            totalTimeSpentLabel.setText(
                    getElapsedTimeLabelMMss( currentProblemContext.totalDuration ) ) ;
        }
    }

    private void clearProblemDetails() {
        SwingUtilities.invokeLater( () -> {
            currentProblemContext = null ;
            bookNameLabel.setText( "" ) ;
            chapterNameLabel.setText( "" ) ;
            problemKeyLabel.setText( "" ) ;
            currentStateBadgeLabel.setText( "" ) ;
            currentProblemTimerLabel.setText( "" ) ;
            totalTimeSpentLabel.setText( "" ) ;
        }) ;
    }

    private String getElapsedTimeLabelMMss( int seconds ) {
        int minutes = seconds / 60 ;
        int secs = seconds % 60 ;
        return String.format( "%02d:%02d", minutes, secs ) ;
    }

    private Color getStateFgColor( String state ) {
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
}
