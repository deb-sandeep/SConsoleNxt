package com.sandy.sconsole.ui.screen.dashboard.tile;

import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.state.PastEffortProvider;
import com.sandy.sconsole.state.manager.PastEffortProviderManager;
import com.sandy.sconsole.ui.util.HistoricDayValueChart;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartPanel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.swing.border.MatteBorder;
import java.awt.*;

import static com.sandy.sconsole.EventCatalog.PAST_EFFORT_UPDATED;

@Slf4j
@Component
@Scope( "prototype" )
public class TotalL60EffortTile extends Tile
    implements EventSubscriber {
    
    @Autowired private EventBus                  eventBus ;
    @Autowired private PastEffortProviderManager pastStudyTimesManager ;
    
    private HistoricDayValueChart dayValueChart ;
    
    @Override
    public void initialize() {
        subscribeToEvents() ;
        setBorder( new MatteBorder( 0, 1, 1, 1, UITheme.TILE_BORDER_COLOR ) ) ;
        
        PastEffortProvider pastStudyTimes = pastStudyTimesManager.getPastEffortProvider() ;
        dayValueChart = new HistoricDayValueChart( "Hours",
                                            Color.DARK_GRAY,
                                            Color.LIGHT_GRAY,
                                            pastStudyTimes,
                                            null,
                                            true ) ;
        
        ChartPanel chartPanel = new ChartPanel( dayValueChart.getJFreeChart() ) ;
        chartPanel.setMinimumDrawHeight( 204 ) ;
        chartPanel.setMinimumDrawWidth( 1920 ) ;
        chartPanel.setMaximumDrawHeight( 204 ) ;
        chartPanel.setMaximumDrawWidth( 1920 ) ;
        
        add( chartPanel, BorderLayout.CENTER ) ;
    }
    
    private void subscribeToEvents() {
        eventBus.addAsyncSubscriber( this, PAST_EFFORT_UPDATED ) ;
    }
    
    @Override
    public void handleEvent( Event event ) {
        dayValueChart.refreshChart() ;
    }
}
