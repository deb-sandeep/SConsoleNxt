package com.sandy.sconsole.ui.screen.session.tile;

import com.sandy.sconsole.EventCatalog;
import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.dao.session.repo.ProblemAttemptRepo;
import com.sandy.sconsole.state.TopicL30BurnProvider;
import com.sandy.sconsole.ui.util.DayValueChart;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartPanel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;

@Slf4j
@Component
@Scope( "prototype" )
public class TopicL30BurnTile extends Tile
        implements EventSubscriber {
    
    @Autowired
    private EventBus eventBus ;
    
    @Autowired
    private ProblemAttemptRepo problemAttemptRepo ;
    
    @Setter
    private Integer topicId ;
    
    private TopicL30BurnProvider pastBurnProvider ;
    
    private DayValueChart dayValueChart ;
    private ChartPanel chartPanel ;
    
    @Override
    public void beforeActivation() {
        subscribeToEvents() ;
        if( chartPanel == null ) {
            createNewDayValueChart() ;
        }
        else if( pastBurnProvider != null && topicId != pastBurnProvider.getTopicId() ) {
            createNewDayValueChart() ;
        }
        else {
            dayValueChart.refreshChart() ;
        }
    }
    
    private void subscribeToEvents() {
        eventBus.addAsyncSubscriber( this, EventCatalog.PROBLEM_ATTEMPT_ENDED ) ;
    }
    
    @Override
    public void handleEvent( Event event ) {
        pastBurnProvider.updateTodayValue() ;
        dayValueChart.refreshChart() ;
    }

    private void createNewDayValueChart() {
        
        pastBurnProvider = new TopicL30BurnProvider( topicId, problemAttemptRepo ) ;
        pastBurnProvider.fullRefresh() ;
        
        dayValueChart = new DayValueChart( "Hours",
                                    Color.BLUE,
                                    Color.LIGHT_GRAY,
                                    pastBurnProvider,
                                    null,
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
