package com.sandy.sconsole.ui.screen.dashboard.tile.burn;

import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.state.ActiveTopicStatistics;
import com.sandy.sconsole.state.manager.ActiveTopicStatisticsManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sandy.sconsole.EventCatalog.ATS_MANAGER_REFRESHED;
import static com.sandy.sconsole.EventCatalog.ATS_REFRESHED;

/**
 * This tile depicts the active topics for a given syllabus along with the
 * following information:
 * - A percentage completion bar, showing the percentage of problems in
 *   this topic completed and percentage remaining
 * - A burn meter for each topic depicting
 *      - Target problem count for today
 *      - Number of problems solved today (color coded - red, amber and green)
 *
 * <p>
 * Reactive features:
 * ------------------
 * - Lifecycle events:
 *      - Setting up of syllabus name for this tile
 *      - initialize() : sets up the UI and does the initial refresh
 *
 * - Event ATS_MANAGER_REFRESHED : Do a full refresh. Day might have changed
 * - Event ATS_REFRESHED : Update the topic burn
 */
@Slf4j
@Component
@Scope( "prototype" )
public class SyllabusBurnTile extends Tile
    implements EventSubscriber {

    private static final int[] SUBSCRIBED_EVENTS = {
        ATS_MANAGER_REFRESHED,
        ATS_REFRESHED
    } ;
    
    @Autowired private EventBus eventBus ;
    @Autowired private ActiveTopicStatisticsManager atsManager ;
    @Autowired private TopicBurnPanel topBurnPanel ;
    @Autowired private TopicBurnPanel bottomBurnPanel ;
    
    @Getter @Setter private String syllabusName ;
    
    private final Map<Integer, TopicBurnPanel> burnPanelMap = new HashMap<>() ;
    
    @Override
    public void initialize() {
        eventBus.addSubscriberForEventTypes( this, false, SUBSCRIBED_EVENTS ) ;
        setBorder( new MatteBorder( 1, 1, 0, 1, UITheme.TILE_BORDER_COLOR ) ) ;
        setLayout( new GridLayout( 2, 1 ) ) ;
        
        add( topBurnPanel, 0 ) ;
        add( bottomBurnPanel, 1 ) ;
        
        refresh() ;
    }
    
    @Override
    public void handleEvent( Event event ) {
        switch( event.getEventType() ) {
            case ATS_MANAGER_REFRESHED -> refresh() ;
            case ATS_REFRESHED -> refreshTopicBurn( (int)event.getValue() ) ;
        }
    }
    
    private void refresh() {
        log.debug( "Refreshing syllabus burn tile" ) ;
        
        burnPanelMap.clear() ;
        topBurnPanel.setTopicStats( null ) ;
        bottomBurnPanel.setTopicStats( null ) ;
        
        List<ActiveTopicStatistics> topicStats = atsManager.getTopicStatistics( syllabusName ) ;
        if( !topicStats.isEmpty() ) {
            ActiveTopicStatistics stat = topicStats.get( 0 ) ;
            topBurnPanel.setTopicStats( stat ) ;
            burnPanelMap.put( stat.getTopic().getTopicId(), topBurnPanel ) ;
        }
        
        if( topicStats.size() > 1 ) {
            ActiveTopicStatistics stat = topicStats.get( 1 ) ;
            bottomBurnPanel.setTopicStats( stat ) ;
            burnPanelMap.put( stat.getTopic().getTopicId(), bottomBurnPanel ) ;
        }
        
        topBurnPanel.refreshUI() ;
        bottomBurnPanel.refreshUI() ;
    }
    
    private void refreshTopicBurn( int topicId ) {
        TopicBurnPanel topicBurnPanel = burnPanelMap.get( topicId ) ;
        // Can the topic burn panel be null? Yes, note that this instance
        // will receive ATS_REFRESHED message for all active topics and
        // this tile manages topic tiles only for the given syllabus
        if( topicBurnPanel != null ) {
            topicBurnPanel.refreshUI() ;
        }
    }
}
