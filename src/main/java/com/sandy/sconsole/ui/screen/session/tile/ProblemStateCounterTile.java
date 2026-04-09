package com.sandy.sconsole.ui.screen.session.tile;

import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.state.ActiveTopicStatistics;
import com.sandy.sconsole.state.manager.ActiveTopicStatisticsManager;
import com.sandy.sconsole.state.manager.ProblemStateCounter;
import info.clearthought.layout.TableLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

import static com.sandy.sconsole.EventCatalog.ATS_REFRESHED;

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
    
    static final String[] COLUMN_HEADERS = {
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

    private final ProblemStateCounterRowPanel allCountRowPanel =
            new ProblemStateCounterRowPanel( "All", 1 ) ;
    
    private final ProblemStateCounterRowPanel todayCountRowPanel =
            new ProblemStateCounterRowPanel( "Today", 1 ) ;
    
    public ProblemStateCounterTile() {
        setUpUI() ;
    }
    
    private void setUpUI() {
        
        TableLayout layout = new TableLayout() ;
        layout.insertColumn( 0, TableLayout.FILL ) ;
        layout.insertRow( 0, HEADER_ROW_HEIGHT ) ;
        layout.insertRow( 1, BODY_ROW_HEIGHT ) ;
        layout.insertRow( 2, BODY_ROW_HEIGHT ) ;
        setLayout( layout ) ;

        add( ProblemStateCounterRowPanel.createHeaderPanel(), "0,0" ) ;
        add( allCountRowPanel, "0,1" ) ;
        add( todayCountRowPanel, "0,2" ) ;
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
                allCountRowPanel.setCounter( null ) ;
                todayCountRowPanel.setCounter( null ) ;
            }
            else {
                allCountRowPanel.setCounter( ats.getAllProblemsStateCounter() ) ;
                todayCountRowPanel.setCounter( ats.getTodayProblemsStateCounter() ) ;
            }
        } ) ;
    }
}
