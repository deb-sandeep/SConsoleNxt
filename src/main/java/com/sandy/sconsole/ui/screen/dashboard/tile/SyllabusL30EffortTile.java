package com.sandy.sconsole.ui.screen.dashboard.tile;

import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.core.ui.uiutil.SwingUtils;
import com.sandy.sconsole.state.SyllabusPastEffortProvider;
import com.sandy.sconsole.state.manager.PastEffortProviderManager;
import com.sandy.sconsole.ui.util.ConfiguredUIAttributes;
import com.sandy.sconsole.ui.util.DayValueChart;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartPanel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;

import static com.sandy.sconsole.EventCatalog.PAST_EFFORT_UPDATED;

@Slf4j
@Component
@Scope( "prototype" )
public class SyllabusL30EffortTile extends Tile
        implements EventSubscriber {
    
    @Autowired
    private EventBus eventBus ;
    
    @Autowired
    private ConfiguredUIAttributes uiAttributes ;
    
    @Autowired
    private PastEffortProviderManager pastEffortsManager;
    
    @Setter
    private String syllabusName ;
    
    private SyllabusPastEffortProvider pastEffortProvider;
    
    private DayValueChart dayValueChart ;
    private ChartPanel    chartPanel ;
    
    @Override
    public void beforeActivation() {
        subscribeToEvents() ;
        if( chartPanel == null ) {
            createNewDayValueChart() ;
        }
        else if( pastEffortProvider != null && !syllabusName.equals( pastEffortProvider.getSyllabusName() ) ) {
            createNewDayValueChart() ;
        }
        else {
            dayValueChart.refreshChart() ;
        }
    }
    
    private void subscribeToEvents() {
        eventBus.addAsyncSubscriber( this, PAST_EFFORT_UPDATED ) ;
    }
    
    @Override
    public void handleEvent( Event event ) {
        dayValueChart.refreshChart() ;
    }

    private void createNewDayValueChart() {
        
        Color syllabusColor = uiAttributes.getSyllabusColor( syllabusName ) ;
        
        pastEffortProvider = pastEffortsManager.getPastEffortProvider( syllabusName ) ;
        
        dayValueChart = new DayValueChart( "Hours",
                                    SwingUtils.darkerColor( syllabusColor, 0.5F ),
                                    syllabusColor.brighter(), 
                                    pastEffortProvider,
                                    pastEffortsManager::getMaxSyllabusTime,
                                    true ) ;
        
        if( chartPanel != null ) {
            remove( chartPanel ) ;
        }
        chartPanel = new ChartPanel( dayValueChart.getJFreeChart() ) ;
        add( chartPanel, BorderLayout.CENTER ) ;
    }
    
    @Override
    public void beforeDeactivation() {
        eventBus.removeSubscriber( this ) ;
    }
    
}
