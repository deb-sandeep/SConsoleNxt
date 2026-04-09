package com.sandy.sconsole.ui.screen.session.tile;

import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.state.ActiveTopicStatistics;
import com.sandy.sconsole.state.manager.ActiveTopicStatisticsManager;
import com.sandy.sconsole.state.manager.ProblemStateCounter;
import info.clearthought.layout.TableLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

import static com.sandy.sconsole.EventCatalog.ATS_REFRESHED;
import static javax.swing.SwingConstants.CENTER;
import static javax.swing.SwingConstants.RIGHT;

@Component
@Scope( "prototype" )
public class ProblemStateCounterTile extends Tile
        implements EventSubscriber {

    public interface CounterValueProvider {
        int getValue( ProblemStateCounter counter ) ;
    }
    
    public static int getNumPigeons( ProblemStateCounter counter ) {
        return counter.getNumPigeons() + counter.getNumPigeonsSolved() ;
    }
    
    public static int getNumIncorrect( ProblemStateCounter counter ) {
        return counter.getNumIncorrect() + counter.getNumPigeonsExplained() ;
    }
    
    private static final float HEADER_ROW_HEIGHT = 0.20F ;
    public  static final float BODY_ROW_HEIGHT = ( 1.0F - HEADER_ROW_HEIGHT ) / 2.0F ;
    
    public static final Font HEADER_FONT = new Font( Font.MONOSPACED, Font.PLAIN, 12 ) ;
    public static final Font VALUE_FONT  = new Font( Font.MONOSPACED, Font.PLAIN, 27 ) ;
    
    public static final Color GRID_COLOR      = new Color( 21, 21, 21 ) ;
    public static final Color LABEL_FG_COLOR  = Color.WHITE ;
    public static final Color HDR_FG_COLOR    = Color.DARK_GRAY ;
    
    public static final int COUNTER_CELL_RIGHT_INSET = 15 ;
    
    private static final String ALL_SCOPE_LABEL = "All" ;
    private static final String TODAY_SCOPE_LABEL = "Today" ;
    
    private static final String[] COLUMN_HEADERS = {
        "Scope", "Total", "Correct", "Wrong", "Later",
        "Redo", "Pigeon", "Purged", "Reassign"
    } ;
    
    public static final int NUM_COUNTER_COLUMNS = COLUMN_HEADERS.length ;
    public static final float COUNTER_COL_WIDTH = 1.0F / NUM_COUNTER_COLUMNS ;
    
    public static final Color[] COLUMN_VALUE_COLORS = {
        new Color( 106, 106, 106 ),    // Total
        new Color( 0x7C, 0xE3, 0x8B ), // Correct
        new Color( 255, 61, 28 ),      // Wrong
        new Color( 0x8E, 0xB8, 0xFF ), // Later
        new Color( 175, 122, 62 ),     // Redo
        new Color( 255, 220, 74 ),     // Pigeon
        new Color( 87, 87, 87 ),       // Purged
        new Color( 251, 18, 145 ),     // Reassign
    } ;
    
    public static final CounterValueProvider[] COUNTER_VALUE_PROVIDERS = {
        ProblemStateCounter::getTotalCount,
        ProblemStateCounter::getNumCorrect,
        ProblemStateCounterTile::getNumIncorrect,
        ProblemStateCounter::getNumLater,
        ProblemStateCounter::getNumRedo,
        ProblemStateCounterTile::getNumPigeons,
        ProblemStateCounter::getNumPurged,
        ProblemStateCounter::getNumReassign,
    } ;
    
    @Autowired
    private ActiveTopicStatisticsManager atsManager ;
    
    @Autowired
    private EventBus eventBus ;
    
    private ActiveTopicStatistics ats = null ;
    
    private final JLabel[] allCountLabels = new JLabel[COUNTER_VALUE_PROVIDERS.length] ;
    private final JLabel[] todayCountLabels = new JLabel[COUNTER_VALUE_PROVIDERS.length] ;
    
    public ProblemStateCounterTile() {
        setUpUI() ;
    }
    
    private void setUpUI() {
        
        TableLayout layout = new TableLayout() ;
        layout.insertRow( 0, HEADER_ROW_HEIGHT ) ;
        layout.insertRow( 1, BODY_ROW_HEIGHT ) ;
        layout.insertRow( 2, BODY_ROW_HEIGHT ) ;
        
        for( int i=0; i<COLUMN_HEADERS.length; i++ ) {
            layout.insertColumn( i, COUNTER_COL_WIDTH ) ;
        }
        setLayout( layout ) ;
        
        for( int col=0; col<COLUMN_HEADERS.length; col++ ) {
            JLabel headerLabel = createCellLabel( COLUMN_HEADERS[col], true ) ;
            add( headerLabel, col + ",0" ) ;
        }
        
        add( createScopeLabel( ALL_SCOPE_LABEL ), "0,1" ) ;
        add( createScopeLabel( TODAY_SCOPE_LABEL ), "0,2" ) ;
        
        for( int i=0; i<COUNTER_VALUE_PROVIDERS.length; i++ ) {
            JLabel allCountLabel = createCellLabel( "", false ) ;
            JLabel todayCountLabel = createCellLabel( "", false ) ;
            
            allCountLabels[i] = allCountLabel ;
            todayCountLabels[i] = todayCountLabel ;
            
            add( allCountLabel, ( i+1 ) + ",1" ) ;
            add( todayCountLabel, ( i+1 ) + ",2" ) ;
        }
    }
    
    public void setTopicId( int topicId ) {
        this.ats = atsManager.getTopicStatistics( topicId ) ;
    }
    
    @Override
    public void beforeActivation() {
        if( ats == null ) {
            throw new RuntimeException( "No active topic statistics found" ) ;
        }
        eventBus.addAsyncSubscriber( this, ATS_REFRESHED ) ;
        refreshCounts() ;
    }
    
    @Override
    public void beforeDeactivation() {
        eventBus.removeSubscriber( this ) ;
    }
    
    @Override
    public void handleEvent( Event event ) {
        if( event.getEventId() == ATS_REFRESHED ) {
            refreshCounts() ;
        }
    }
    
    private void refreshCounts() {
        SwingUtilities.invokeLater( () -> {
            if( ats == null ) {
                clearCountLabels() ;
            }
            else {
                updateCountLabels( allCountLabels, ats.getAllProblemsStateCounter() ) ;
                updateCountLabels( todayCountLabels, ats.getTodayProblemsStateCounter() ) ;
            }
        } ) ;
    }
    
    private JLabel createScopeLabel( String text ) {
        return createCellLabel( text, true ) ;
    }
    
    private JLabel createCellLabel( String text, boolean isHeader ) {
        
        JLabel label = new JLabel( text ) ;
        label.setHorizontalAlignment( RIGHT ) ;
        label.setVerticalAlignment( CENTER ) ;
        label.setOpaque( true ) ;
        label.setForeground( LABEL_FG_COLOR ) ;
        label.setBackground( UITheme.BG_COLOR ) ;
        label.setFont( isHeader ? HEADER_FONT : VALUE_FONT ) ;
        label.setBorder(
            BorderFactory.createCompoundBorder(
                new MatteBorder( 1, 1, 1, 1, GRID_COLOR ),
                BorderFactory.createEmptyBorder( 0, 0, 0, COUNTER_CELL_RIGHT_INSET )
            )
        ) ;
        label.setForeground( isHeader ? HDR_FG_COLOR : LABEL_FG_COLOR ) ;
        return label ;
    }
    
    private void clearCountLabels() {
        updateCountLabels( allCountLabels, null ) ;
        updateCountLabels( todayCountLabels, null ) ;
    }
    
    private void updateCountLabels( JLabel[] labels, ProblemStateCounter counter ) {
        for( int i=0; i<COUNTER_VALUE_PROVIDERS.length; i++ ) {
            if( counter == null ) {
                labels[i].setText( "" ) ;
                labels[i].setForeground( HDR_FG_COLOR ) ;
            }
            else {
                int value = COUNTER_VALUE_PROVIDERS[i].getValue( counter ) ;
                labels[i].setText( String.valueOf( value ) ) ;
                labels[i].setForeground( value == 0 ? HDR_FG_COLOR : COLUMN_VALUE_COLORS[i] ) ;
            }
        }
    }
}
