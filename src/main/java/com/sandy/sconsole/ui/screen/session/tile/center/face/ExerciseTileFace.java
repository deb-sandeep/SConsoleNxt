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
import com.sandy.sconsole.state.manager.ProblemStateCounter;
import com.sandy.sconsole.state.manager.TodaySessionStatistics;
import com.sandy.sconsole.ui.screen.session.tile.ProblemStateCounterTile;
import info.clearthought.layout.TableLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

import static com.sandy.sconsole.EventCatalog.*;
import static com.sandy.sconsole.core.ui.uiutil.UITheme.BG_COLOR;
import static com.sandy.sconsole.ui.screen.session.tile.ProblemStateCounterTile.*;
import static javax.swing.SwingConstants.CENTER;
import static javax.swing.SwingConstants.RIGHT;

@Component
@Scope( "prototype" )
public class ExerciseTileFace extends Tile
    implements EventSubscriber {

    private static final Font COUNTER_VALUE_FONT = ProblemStateCounterTile.VALUE_FONT ;
    
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
    private Problem currentProblem = null ;
    private ProblemAttemptDTO currentProblemAttempt = null ;
    private int totalProblemAttempts = 0 ;
    private int totalAttemptTimeBeforeCurrent = 0 ;

    private final JLabel[] sessionCountLabels = new JLabel[COUNTER_VALUE_PROVIDERS.length] ;

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
        
        add( createSessionCounterPanel(), "0,0" ) ;
        add( configureLabel( bookNameLabel, BOOK_FONT, BOOK_NAME_COLOR ), "0,1" ) ;
        add( createChapterDetailPanel(), "0,2" ) ;
        add( createCurrentProblemInsightPanel(), "0,3" ) ;
    }

    private JPanel createSessionCounterPanel() {

        JPanel panel = new JPanel() ;
        panel.setBackground( BG_COLOR ) ;

        TableLayout layout = new TableLayout() ;
        layout.insertRow( 0, TableLayout.FILL ) ;
        for( int i=0; i<ProblemStateCounterTile.NUM_COUNTER_COLUMNS; i++ ) {
            layout.insertColumn( i, ProblemStateCounterTile.COUNTER_COL_WIDTH ) ;
        }
        panel.setLayout( layout ) ;

        panel.add( createCounterCellLabel( "Session", HEADER_FONT ), "0,0" ) ;

        for( int i=0; i<COUNTER_VALUE_PROVIDERS.length; i++ ) {
            JLabel label = createCounterCellLabel( "", COUNTER_VALUE_FONT ) ;
            sessionCountLabels[i] = label ;
            panel.add( label, ( i+1 ) + ",0" ) ;
        }
        return panel ;
    }

    private JLabel createCounterCellLabel( String text, Font font ) {

        JLabel label = new JLabel( text ) ;
        label.setHorizontalAlignment( RIGHT ) ;
        label.setVerticalAlignment( CENTER ) ;
        label.setOpaque( true ) ;
        label.setForeground( HDR_FG_COLOR ) ;
        label.setBackground( BG_COLOR ) ;
        label.setFont( font ) ;
        label.setBorder( BorderFactory.createCompoundBorder(
                new MatteBorder( 0, 1, 1, 1, GRID_COLOR ),
                BorderFactory.createEmptyBorder( 0, 0, 0, COUNTER_CELL_RIGHT_INSET )
        ) ) ;
        return label ;
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
            case PROBLEM_ATTEMPT_STARTED -> handleProblemAttemptStarted( event ) ;
            case SESSION_EXTENDED -> handleSessionExtended( event ) ;
        }
    }

    private void handleATSRefreshed( Event event ) {

        Integer refreshedTopicId = ( Integer )event.getValue() ;
        if( refreshedTopicId == ats.getTopicId() ) {
            refreshSessionCounts() ;
        }
    }

    private void handleProblemAttemptStarted( Event event ) {

        ProblemAttemptDTO attempt = ( ProblemAttemptDTO )event.getValue() ;
        Problem problem = problemRepo.findById( attempt.getProblemId() ).orElse( null ) ;
        loadCurrentProblemContext( problem, attempt ) ;
    }

    private void handleSessionExtended( Event event ) {

        SessionExtensionVO extension = ( SessionExtensionVO )event.getValue() ;
        ProblemAttemptDTO attempt = extension.getProblemAttemptDTO() ;

        if( currentProblem == null ||
            currentProblemAttempt == null ||
            !attempt.getId().equals( currentProblemAttempt.getId() ) ) {

            Problem problem = problemRepo.findById( attempt.getProblemId() ).orElse( null ) ;
            loadCurrentProblemContext( problem, attempt ) ;
        }
        else {
            currentProblemAttempt = attempt ;
            refreshProblemDetails() ;
        }
    }

    private void refreshSessionCounts() {

        SwingUtilities.invokeLater( () -> {
            ProblemStateCounter counter = ats == null ? null : ats.getCurrentSessionProblemStates() ;
            for( int i=0; i<COUNTER_VALUE_PROVIDERS.length; i++ ) {
                JLabel label = sessionCountLabels[i] ;
                if( counter == null ) {
                    label.setText( "" ) ;
                    label.setForeground( HDR_FG_COLOR ) ;
                }
                else {
                    int value = COUNTER_VALUE_PROVIDERS[i].getValue( counter ) ;
                    label.setText( String.valueOf( value ) ) ;
                    label.setForeground( value == 0 ? HDR_FG_COLOR : COLUMN_VALUE_COLORS[i] ) ;
                }
            }
        } ) ;
    }

    private void loadCurrentProblemContext( Problem problem, ProblemAttemptDTO attempt ) {

        this.currentProblem = problem ;
        this.currentProblemAttempt = attempt ;

        if( problem == null || attempt == null ) {
            this.totalProblemAttempts = 0 ;
            this.totalAttemptTimeBeforeCurrent = 0 ;
            clearProblemDetails() ;
            return ;
        }

        Integer numAttempts = problemAttemptRepo.getNumAttempts( problem.getId() ) ;
        Integer totalAttemptTime = problemAttemptRepo.getTotalAttemptTime( problem.getId() ) ;
        int currentAttemptTime = getEffectiveDuration( attempt ) ;

        this.totalProblemAttempts = numAttempts == null ? 0 : numAttempts ;
        this.totalAttemptTimeBeforeCurrent = totalAttemptTime == null ? 0 : totalAttemptTime ;
        this.totalAttemptTimeBeforeCurrent =
                Math.max( 0, this.totalAttemptTimeBeforeCurrent - currentAttemptTime ) ;

        refreshProblemDetails() ;
    }

    private void refreshProblemDetails() {

        SwingUtilities.invokeLater( () -> {
            if( currentProblem == null || currentProblemAttempt == null ) {
                clearProblemDetails() ;
                return ;
            }

            String bookShortName = currentProblem.getChapter().getBook().getBookShortName() ;
            Chapter chapter = currentProblem.getChapter() ;
            String chapterName = chapter.getId().getChapterNum() + ". " +
                                 chapter.getChapterName() ;
            String problemKey = "[" + currentProblem.getProblemKey() + "]" ;
            int currentAttemptTime = getEffectiveDuration( currentProblemAttempt ) ;
            int totalAttemptTime = totalAttemptTimeBeforeCurrent + currentAttemptTime ;
            
            bookNameLabel.setText( bookShortName ) ;
            chapterNameLabel.setText( chapterName ) ;
            problemKeyLabel.setText( problemKey ) ;
            currentStateBadgeLabel.setText( "[" + totalProblemAttempts + "] " + currentProblemAttempt.getPrevState() ) ;
            currentStateBadgeLabel.setForeground( getStateForegroundColor( currentProblemAttempt.getPrevState() ) ) ;
            currentProblemTimerLabel.setText( getElapsedTimeLabelMMss( currentAttemptTime ) ) ;
            totalTimeSpentLabel.setText( getElapsedTimeLabelMMss( totalAttemptTime ) ) ;
        } ) ;
    }

    private void clearProblemDetails() {
        this.currentProblem = null ;
        this.currentProblemAttempt = null ;
        this.totalProblemAttempts = 0 ;
        this.totalAttemptTimeBeforeCurrent = 0 ;

        SwingUtilities.invokeLater( () -> {
            bookNameLabel.setText( "" ) ;
            chapterNameLabel.setText( "" ) ;
            problemKeyLabel.setText( "" ) ;
            currentStateBadgeLabel.setText( "" ) ;
            currentProblemTimerLabel.setText( "" ) ;
            totalTimeSpentLabel.setText( "" ) ;
        } ) ;
    }

    private int getEffectiveDuration( ProblemAttemptDTO attempt ) {
        return attempt.getEffectiveDuration() == null ? 0 : attempt.getEffectiveDuration() ;
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
}
