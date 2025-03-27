package com.sandy.sconsole.ui.screen.dashboard.tile;

import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.core.ui.uiutil.SwingUtils;
import com.sandy.sconsole.state.SyllabusPastStudyTimesProvider;
import com.sandy.sconsole.state.manager.PastStudyTimesProviderManager;
import com.sandy.sconsole.ui.util.ConfiguredUIAttributes;
import com.sandy.sconsole.ui.util.HistoricDayValueChart;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartPanel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;

import static com.sandy.sconsole.EventCatalog.PAST_STUDY_TIME_UPDATED;

@Slf4j
@Component
@Scope( "prototype" )
public class SyllabusL30EffortTile extends Tile
        implements EventSubscriber {
    
    private static final int[] SUBSCRIBED_EVENTS = {
       PAST_STUDY_TIME_UPDATED,
    } ;
    
    @Autowired private EventBus eventBus ;
    @Autowired private ConfiguredUIAttributes        uiAttributes ;
    @Autowired private PastStudyTimesProviderManager pastStudyTimesManager ;
    
    @Setter private String syllabusName ;
    
    private SyllabusPastStudyTimesProvider pastStudyTimesProvider;
    private HistoricDayValueChart dayValueChart ;
    private ChartPanel chartPanel ;
    
    @Override
    public void beforeActivation() {
        eventBus.addSubscriber( this, true, SUBSCRIBED_EVENTS ) ;
        if( chartPanel == null ) {
            createNewDayValueChart() ;
        }
        else if( pastStudyTimesProvider != null && !syllabusName.equals( pastStudyTimesProvider.getSyllabusName() ) ) {
            createNewDayValueChart() ;
        }
        else {
            dayValueChart.refreshChart() ;
        }
    }
    
    private void createNewDayValueChart() {
        
        Color syllabusColor = uiAttributes.getSyllabusColor( syllabusName ) ;
        pastStudyTimesProvider = pastStudyTimesManager.getPastStudyTimesProvider( syllabusName ) ;
        dayValueChart = new HistoricDayValueChart( "Hours",
                                    SwingUtils.darkerColor( syllabusColor, 0.5F ),
                                    syllabusColor.brighter(),
                                    pastStudyTimesProvider,
                                    pastStudyTimesManager::getMaxSyllabusTime,
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
    
    @Override
    public void handleEvent( Event event ) {
        dayValueChart.refreshChart() ;
    }
}
