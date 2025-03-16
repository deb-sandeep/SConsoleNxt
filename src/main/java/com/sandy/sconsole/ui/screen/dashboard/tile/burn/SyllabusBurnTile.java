package com.sandy.sconsole.ui.screen.dashboard.tile.burn;

import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.ui.util.ActiveTopicStatistics;
import com.sandy.sconsole.ui.util.ActiveTopicStatisticsManager;
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

@Slf4j
@Component
@Scope( "prototype" )
public class SyllabusBurnTile extends Tile
    implements EventSubscriber {

    private static final int[] SUBSCRIBED_EVENTS = { ATS_MANAGER_REFRESHED } ;
    
    @Autowired private EventBus eventBus ;
    @Autowired private ActiveTopicStatisticsManager atsManager ;
    @Autowired private TopicBurnPanel topBurnPanel ;
    @Autowired private TopicBurnPanel bottomBurnPanel ;
    
    @Getter @Setter private String syllabusName ;
    
    private final Map<Integer, ActiveTopicStatistics> statsMap = new HashMap<>() ;
    
    private final Map<Integer, TopicBurnPanel> burnPanelMap = new HashMap<>() ;
    
    @Override
    public void initialize() {
        eventBus.addSubscriberForEventTypes( this, false, SUBSCRIBED_EVENTS ) ;
        setBorder( new MatteBorder( 1, 1, 0, 1, theme.getTileBorderColor() ) ) ;
        setLayout( new GridLayout( 2, 1 ) ) ;
        
        add( topBurnPanel, 0 ) ;
        add( bottomBurnPanel, 1 ) ;
        
        refresh() ;
    }
    
    @Override
    public void handleEvent( Event event ) {
        switch( event.getEventType() ) {
            case ATS_MANAGER_REFRESHED -> refresh() ;
        }
    }
    
    private void refresh() {
        log.debug( "Refreshing syllabus burn tile" ) ;
        
        burnPanelMap.clear() ;
        topBurnPanel.setTopicStats( null ) ;
        bottomBurnPanel.setTopicStats( null ) ;
        
        List<ActiveTopicStatistics> topicStats = atsManager.getTopicStatstics( syllabusName ) ;
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
}
