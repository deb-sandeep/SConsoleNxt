package com.sandy.sconsole.ui.screen.session.tile.center.face;

import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.dao.master.Problem;
import com.sandy.sconsole.dao.master.repo.ProblemRepo;
import com.sandy.sconsole.dao.session.dto.ProblemAttemptDTO;
import com.sandy.sconsole.state.ActiveTopicStatistics;
import com.sandy.sconsole.state.manager.ActiveTopicStatisticsManager;
import com.sandy.sconsole.state.manager.ProblemStateCounter;
import com.sandy.sconsole.state.manager.TodaySessionStatistics;
import info.clearthought.layout.TableLayout;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import java.awt.*;

import static com.sandy.sconsole.EventCatalog.ATS_REFRESHED;
import static com.sandy.sconsole.EventCatalog.PROBLEM_ATTEMPT_STARTED;
import static javax.swing.SwingConstants.*;

@Slf4j
@Component
@Scope( "prototype" )
public class ExerciseTileFace extends Tile
    implements EventSubscriber {

    private interface CounterValueProvider {
        int getValue( ProblemStateCounter counter ) ;
    }

    private static int getNumPigeons( ProblemStateCounter counter ) {
        return counter.getNumPigeons() + counter.getNumPigeonsSolved() ;
    }

    private static int getNumIncorrect( ProblemStateCounter counter ) {
        return counter.getNumIncorrect() + counter.getNumPigeonsExplained() ;
    }

    private static final float SESSION_COUNTER_ROW_HEIGHT = 1.0F / 3.0F ;
    private static final float DETAIL_ROW_HEIGHT = ( 1.0F - SESSION_COUNTER_ROW_HEIGHT ) / 3.0F ;

    private static final float COUNTER_COL_WIDTH = 1.0F / 9.0F ;
    private static final float META_COL_WIDTH = 1.0F / 3.0F ;

    private static final Font SCOPE_FONT = new Font( Font.MONOSPACED, Font.PLAIN, 12 ) ;
    private static final Font COUNTER_VALUE_FONT = new Font( Font.MONOSPACED, Font.PLAIN, 27 ) ;
    private static final Font BOOK_FONT = new Font( Font.MONOSPACED, Font.PLAIN, 40 ) ;
    private static final Font CHAPTER_FONT = new Font( Font.MONOSPACED, Font.PLAIN, 26 ) ;
    private static final Font META_FONT = new Font( Font.MONOSPACED, Font.PLAIN, 20 ) ;

    private static final Color BG_COLOR = Color.BLACK ;
    private static final Color GRID_COLOR = Color.DARK_GRAY ;
    private static final Color SUBDUED_FG_COLOR = Color.DARK_GRAY ;
    private static final Color DETAIL_FG_COLOR = Color.DARK_GRAY ;

    private static final String SESSION_SCOPE_LABEL = "Session" ;
    private static final String EXERCISE_PREFIX = "Ex " ;
    private static final String TYPE_PREFIX = "Type " ;
    private static final String KEY_PREFIX = "Key " ;

    private static final int COUNTER_RIGHT_INSET = 15 ;
    private static final int DETAIL_LEFT_INSET = 20 ;
    private static final int DETAIL_RIGHT_INSET = 20 ;
    private static final int META_RIGHT_INSET = 12 ;

    private static final Color[] COLUMN_VALUE_COLORS = {
            Color.DARK_GRAY, // Total
            Color.DARK_GRAY, // Correct
            Color.DARK_GRAY, // Wrong
            Color.DARK_GRAY, // Later
            Color.DARK_GRAY, // Redo
            Color.DARK_GRAY, // Pigeon
            Color.DARK_GRAY, // Purged
            Color.DARK_GRAY, // Reassign
    } ;

    private static final CounterValueProvider[] COUNTER_VALUE_PROVIDERS = {
            ProblemStateCounter::getTotalCount,
            ProblemStateCounter::getNumCorrect,
            ExerciseTileFace::getNumIncorrect,
            ProblemStateCounter::getNumLater,
            ProblemStateCounter::getNumRedo,
            ExerciseTileFace::getNumPigeons,
            ProblemStateCounter::getNumPurged,
            ProblemStateCounter::getNumReassign,
    } ;

    @Autowired private EventBus eventBus ;
    @Autowired private ProblemRepo problemRepo ;
    @Autowired private TodaySessionStatistics todaySessionStats ;
    @Autowired private ActiveTopicStatisticsManager atsManager ;

    private ActiveTopicStatistics ats = null ;
    private Integer currentSessionId = null ;

    private final JLabel[] sessionCountLabels = new JLabel[COUNTER_VALUE_PROVIDERS.length] ;

    private final JLabel bookNameLabel = new JLabel() ;
    private final JLabel chapterNameLabel = new JLabel() ;
    private final JLabel exerciseLabel = new JLabel() ;
    private final JLabel problemTypeLabel = new JLabel() ;
    private final JLabel problemKeyLabel = new JLabel() ;

    public ExerciseTileFace() {
        setUpUI() ;
    }

    private void setUpUI() {

        setBackground( BG_COLOR ) ;

        TableLayout layout = new TableLayout() ;
        layout.insertColumn( 0, TableLayout.FILL ) ;
        layout.insertRow( 0, SESSION_COUNTER_ROW_HEIGHT ) ;
        layout.insertRow( 1, DETAIL_ROW_HEIGHT ) ;
        layout.insertRow( 2, DETAIL_ROW_HEIGHT ) ;
        layout.insertRow( 3, DETAIL_ROW_HEIGHT ) ;
        setLayout( layout ) ;

        add( createSessionCounterPanel(), "0,0" ) ;
        add( configureDetailLabel( bookNameLabel, BOOK_FONT ), "0,1" ) ;
        add( configureDetailLabel( chapterNameLabel, CHAPTER_FONT ), "0,2" ) ;
        add( createProblemMetaPanel(), "0,3" ) ;
    }

    private JPanel createSessionCounterPanel() {

        JPanel panel = new JPanel() ;
        panel.setBackground( BG_COLOR ) ;

        TableLayout layout = new TableLayout() ;
        layout.insertRow( 0, TableLayout.FILL ) ;
        for( int i=0; i<9; i++ ) {
            layout.insertColumn( i, COUNTER_COL_WIDTH ) ;
        }
        panel.setLayout( layout ) ;

        panel.add( createScopeLabel( SESSION_SCOPE_LABEL ), "0,0" ) ;

        for( int i=0; i<COUNTER_VALUE_PROVIDERS.length; i++ ) {
            JLabel label = createCounterValueLabel() ;
            sessionCountLabels[i] = label ;
            panel.add( label, ( i+1 ) + ",0" ) ;
        }
        return panel ;
    }

    private JPanel createProblemMetaPanel() {

        JPanel panel = new JPanel() ;
        panel.setBackground( BG_COLOR ) ;

        TableLayout layout = new TableLayout() ;
        layout.insertRow( 0, TableLayout.FILL ) ;
        layout.insertColumn( 0, META_COL_WIDTH ) ;
        layout.insertColumn( 1, META_COL_WIDTH ) ;
        layout.insertColumn( 2, META_COL_WIDTH ) ;
        panel.setLayout( layout ) ;

        panel.add( configureMetaLabel( exerciseLabel ), "0,0" ) ;
        panel.add( configureMetaLabel( problemTypeLabel ), "1,0" ) ;
        panel.add( configureMetaLabel( problemKeyLabel ), "2,0" ) ;
        return panel ;
    }

    private JLabel createScopeLabel( String text ) {

        JLabel label = new JLabel( text ) ;
        label.setHorizontalAlignment( RIGHT ) ;
        label.setVerticalAlignment( CENTER ) ;
        label.setOpaque( true ) ;
        label.setForeground( SUBDUED_FG_COLOR ) ;
        label.setBackground( BG_COLOR ) ;
        label.setFont( SCOPE_FONT ) ;
        label.setBorder( createCounterCellBorder() ) ;
        return label ;
    }

    private JLabel createCounterValueLabel() {

        JLabel label = new JLabel( "" ) ;
        label.setHorizontalAlignment( RIGHT ) ;
        label.setVerticalAlignment( CENTER ) ;
        label.setOpaque( true ) ;
        label.setForeground( SUBDUED_FG_COLOR ) ;
        label.setBackground( BG_COLOR ) ;
        label.setFont( COUNTER_VALUE_FONT ) ;
        label.setBorder( createCounterCellBorder() ) ;
        return label ;
    }

    private Border createCounterCellBorder() {
        return BorderFactory.createCompoundBorder(
                new MatteBorder( 0, 1, 1, 1, GRID_COLOR ),
                BorderFactory.createEmptyBorder( 0, 0, 0, COUNTER_RIGHT_INSET )
        ) ;
    }

    private JLabel configureDetailLabel( JLabel label, Font font ) {

        label.setHorizontalAlignment( LEFT ) ;
        label.setVerticalAlignment( CENTER ) ;
        label.setOpaque( true ) ;
        label.setForeground( DETAIL_FG_COLOR ) ;
        label.setBackground( BG_COLOR ) ;
        label.setFont( font ) ;
        label.setBorder( BorderFactory.createEmptyBorder( 0, DETAIL_LEFT_INSET, 0, DETAIL_RIGHT_INSET ) ) ;
        return label ;
    }

    private JLabel configureMetaLabel( JLabel label ) {

        label.setHorizontalAlignment( LEFT ) ;
        label.setVerticalAlignment( CENTER ) ;
        label.setOpaque( true ) ;
        label.setForeground( DETAIL_FG_COLOR ) ;
        label.setBackground( BG_COLOR ) ;
        label.setFont( META_FONT ) ;
        label.setBorder( BorderFactory.createEmptyBorder( 0, DETAIL_LEFT_INSET, 0, META_RIGHT_INSET ) ) ;
        return label ;
    }

    @Override
    public void beforeActivation() {

        this.currentSessionId = todaySessionStats.getCurrentSession().getId() ;
        this.ats = atsManager.getTopicStatistics( todaySessionStats.getCurrentSession().getTopicId() ) ;

        eventBus.addAsyncSubscriber( this, ATS_REFRESHED ) ;
        eventBus.addAsyncSubscriber( this, PROBLEM_ATTEMPT_STARTED ) ;

        refreshSessionCounts() ;
        clearProblemDetails() ;
    }

    @Override
    public void beforeDeactivation() {
        eventBus.removeSubscriber( this ) ;
        this.ats = null ;
        this.currentSessionId = null ;
        clearProblemDetails() ;
    }

    @Override
    public synchronized void handleEvent( Event event ) {

        switch( event.getEventId() ) {
            case ATS_REFRESHED -> handleATSRefreshed( event ) ;
            case PROBLEM_ATTEMPT_STARTED -> handleProblemAttemptStarted( event ) ;
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

    private void handleProblemAttemptStarted( Event event ) {

        if( currentSessionId == null ) {
            return ;
        }

        ProblemAttemptDTO attempt = ( ProblemAttemptDTO )event.getValue() ;
        if( attempt.getSessionId() == null || !currentSessionId.equals( attempt.getSessionId() ) ) {
            return ;
        }

        Problem problem = problemRepo.findById( attempt.getProblemId() ).orElse( null ) ;
        refreshProblemDetails( problem ) ;
    }

    private void refreshSessionCounts() {

        SwingUtilities.invokeLater( () -> {
            ProblemStateCounter counter = ats == null ? null : ats.getCurrentSessionProblemStates() ;
            for( int i=0; i<COUNTER_VALUE_PROVIDERS.length; i++ ) {
                JLabel label = sessionCountLabels[i] ;
                if( counter == null ) {
                    label.setText( "" ) ;
                    label.setForeground( SUBDUED_FG_COLOR ) ;
                }
                else {
                    int value = COUNTER_VALUE_PROVIDERS[i].getValue( counter ) ;
                    label.setText( String.valueOf( value ) ) ;
                    label.setForeground( value == 0 ? SUBDUED_FG_COLOR : COLUMN_VALUE_COLORS[i] ) ;
                }
            }
        } ) ;
    }

    private void refreshProblemDetails( Problem problem ) {

        SwingUtilities.invokeLater( () -> {
            if( problem == null ) {
                clearProblemDetails() ;
                return ;
            }

            String bookShortName = problem.getChapter().getBook().getBookShortName() ;
            if( bookShortName == null || bookShortName.isBlank() ) {
                bookShortName = problem.getChapter().getBook().getBookName() ;
            }

            bookNameLabel.setText( bookShortName ) ;
            chapterNameLabel.setText( problem.getChapter().getChapterName() ) ;
            exerciseLabel.setText( EXERCISE_PREFIX + problem.getExerciseName() ) ;
            problemTypeLabel.setText( TYPE_PREFIX + problem.getProblemType().getProblemType() ) ;
            problemKeyLabel.setText( KEY_PREFIX + problem.getProblemKey() ) ;
        } ) ;
    }

    private void clearProblemDetails() {
        SwingUtilities.invokeLater( () -> {
            bookNameLabel.setText( "" ) ;
            chapterNameLabel.setText( "" ) ;
            exerciseLabel.setText( "" ) ;
            problemTypeLabel.setText( "" ) ;
            problemKeyLabel.setText( "" ) ;
        } ) ;
    }
}
