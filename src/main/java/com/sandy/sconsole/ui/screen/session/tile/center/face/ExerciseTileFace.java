package com.sandy.sconsole.ui.screen.session.tile.center.face;

import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.dao.master.Chapter;
import com.sandy.sconsole.dao.master.Problem;
import com.sandy.sconsole.dao.master.repo.ProblemRepo;
import com.sandy.sconsole.dao.session.dto.ProblemAttemptDTO;
import com.sandy.sconsole.state.ActiveTopicStatistics;
import com.sandy.sconsole.state.manager.ActiveTopicStatisticsManager;
import com.sandy.sconsole.state.manager.ProblemStateCounter;
import com.sandy.sconsole.state.manager.TodaySessionStatistics;
import com.sandy.sconsole.ui.screen.session.tile.ProblemStateCounterTile;
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
import static com.sandy.sconsole.ui.screen.session.tile.ProblemStateCounterTile.COUNTER_VALUE_PROVIDERS;
import static com.sandy.sconsole.ui.screen.session.tile.ProblemStateCounterTile.HEADER_FONT;
import static javax.swing.SwingConstants.CENTER;
import static javax.swing.SwingConstants.RIGHT;

@Slf4j
@Component
@Scope( "prototype" )
public class ExerciseTileFace extends Tile
    implements EventSubscriber {

    private static final Font COUNTER_VALUE_FONT = ProblemStateCounterTile.VALUE_FONT ;
    
    private static final Font BOOK_FONT    = new Font( Font.MONOSPACED, Font.PLAIN, 33 ) ;
    private static final Font CHAPTER_FONT = new Font( Font.MONOSPACED, Font.PLAIN, 30 ) ;
    private static final Font PROBLEM_FONT = new Font( Font.MONOSPACED, Font.PLAIN, 26 ) ;

    private static final Color BG_COLOR         = ProblemStateCounterTile.VALUE_BG_COLOR ;
    private static final Color GRID_COLOR       = ProblemStateCounterTile.GRID_COLOR ;
    private static final Color SUBDUED_FG_COLOR = ProblemStateCounterTile.HDR_FG_COLOR ;
    private static final Color BOOK_NAME_COLOR  = new Color( 70, 210, 250 ) ;
    private static final Color CHP_NAME_COLOR   = new Color( 163, 113, 255 ) ;
    private static final Color PROBLEM_KEY_COLOR= new Color( 255, 131, 5 ) ;

    private static final String SESSION_SCOPE_LABEL = "Session" ;

    private static final int COUNTER_RIGHT_INSET = ProblemStateCounterTile.COUNTER_CELL_RIGHT_INSET ;

    @Autowired private EventBus eventBus ;
    @Autowired private ProblemRepo problemRepo ;
    @Autowired private TodaySessionStatistics todaySessionStats ;
    @Autowired private ActiveTopicStatisticsManager atsManager ;

    private ActiveTopicStatistics ats = null ;
    private Integer currentSessionId = null ;

    private final JLabel[] sessionCountLabels = new JLabel[COUNTER_VALUE_PROVIDERS.length] ;

    private final JLabel bookNameLabel = new JLabel() ;
    private final JLabel chapterNameLabel = new JLabel() ;
    private final JLabel problemKeyLabel = new JLabel() ;

    public ExerciseTileFace() {
        setUpUI() ;
    }

    private void setUpUI() {

        setBackground( BG_COLOR ) ;

        TableLayout layout = new TableLayout() ;
        layout.insertColumn( 0, TableLayout.FILL ) ;
        layout.insertRow( 0, 60 ) ; // Section problem count row
        layout.insertRow( 1, 60 ) ; // Book name row
        layout.insertRow( 2, 55 ) ; // Chapter name row
        layout.insertRow( 3, 55 ) ; // Problem key row
        setLayout( layout ) ;

        add( createSessionCounterPanel(), "0,0" ) ;
        add( configureDetailLabel( bookNameLabel, BOOK_FONT, BOOK_NAME_COLOR ), "0,1" ) ;
        add( configureDetailLabel( chapterNameLabel, CHAPTER_FONT, CHP_NAME_COLOR ), "0,2" ) ;
        add( configureDetailLabel( problemKeyLabel, PROBLEM_FONT, PROBLEM_KEY_COLOR ), "0,3" ) ;
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

        panel.add( createScopeLabel(), "0,0" ) ;

        for( int i=0; i<COUNTER_VALUE_PROVIDERS.length; i++ ) {
            JLabel label = createCounterValueLabel() ;
            sessionCountLabels[i] = label ;
            panel.add( label, ( i+1 ) + ",0" ) ;
        }
        return panel ;
    }

    private JLabel createScopeLabel() {

        JLabel label = new JLabel( ExerciseTileFace.SESSION_SCOPE_LABEL ) ;
        label.setHorizontalAlignment( RIGHT ) ;
        label.setVerticalAlignment( CENTER ) ;
        label.setOpaque( true ) ;
        label.setForeground( SUBDUED_FG_COLOR ) ;
        label.setBackground( ProblemStateCounterTile.HEADER_BG_COLOR ) ;
        label.setFont( HEADER_FONT ) ;
        label.setBorder( createCounterCellBorder() ) ;
        return label ;
    }

    private JLabel createCounterValueLabel() {

        JLabel label = new JLabel( "" ) ;
        label.setHorizontalAlignment( RIGHT ) ;
        label.setVerticalAlignment( CENTER ) ;
        label.setOpaque( true ) ;
        label.setForeground( SUBDUED_FG_COLOR ) ;
        label.setBackground( ProblemStateCounterTile.VALUE_BG_COLOR ) ;
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

    private JLabel configureDetailLabel( JLabel label, Font font, Color fgColor ) {

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
                    label.setForeground(
                            value == 0 ? SUBDUED_FG_COLOR : ProblemStateCounterTile.COLUMN_VALUE_COLORS[i]
                    ) ;
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
            Chapter chapter = problem.getChapter() ;
            String chapterName = chapter.getId().getChapterNum() + ". " +
                                 chapter.getChapterName() ;
            String problemKey = problem.getProblemKey().replace( "/", " / " ) ;
            
            bookNameLabel.setText( bookShortName ) ;
            chapterNameLabel.setText( chapterName ) ;
            problemKeyLabel.setText( problemKey ) ;
        } ) ;
    }

    private void clearProblemDetails() {
        SwingUtilities.invokeLater( () -> {
            bookNameLabel.setText( "" ) ;
            chapterNameLabel.setText( "" ) ;
            problemKeyLabel.setText( "" ) ;
        } ) ;
    }
}
