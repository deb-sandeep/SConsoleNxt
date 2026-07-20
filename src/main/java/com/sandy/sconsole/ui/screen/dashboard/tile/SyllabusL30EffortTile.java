package com.sandy.sconsole.ui.screen.dashboard.tile;

import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.bus.EventTargetMarker;
import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.core.ui.uiutil.SwingUtils;
import com.sandy.sconsole.dao.session.repo.DailyBurnLogRepo;
import com.sandy.sconsole.state.SyllabusL30EffortProvider;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.sandy.sconsole.EventCatalog.BURN_MET_OVERRIDE;
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

    @Autowired
    private DailyBurnLogRepo dailyBurnLogRepo ;

    @Setter
    private String syllabusName ;
    
    private SyllabusL30EffortProvider pastEffortProvider;
    
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
        eventBus.addAsyncSubscriber( this, PAST_EFFORT_UPDATED, BURN_MET_OVERRIDE ) ;
    }

    @Override
    @EventTargetMarker( { PAST_EFFORT_UPDATED, BURN_MET_OVERRIDE } )
    public synchronized void handleEvent( Event event ) {
        dayValueChart.refreshChart() ;
    }

    private void createNewDayValueChart() {
        
        Color syllabusColor = uiAttributes.getSyllabusColor( syllabusName ) ;
        
        pastEffortProvider = pastEffortsManager.getPastEffortProvider( syllabusName ) ;
        
        DayValueChart.DayBurnMetSource burnMetSource = ( start, end ) -> {
            Map<Date, Boolean> result = new HashMap<>() ;
            dailyBurnLogRepo.getSyllabusFullBurnMet( syllabusName, start, end )
                            .forEach( r -> result.put( r.getDate(), r.burnMetAsBoolean() ) ) ;
            return result ;
        } ;

        dayValueChart = new DayValueChart( "Hours",
                                    SwingUtils.darkerColor( syllabusColor, 0.5F ),
                                    syllabusColor.brighter(),
                                    pastEffortProvider,
                                    pastEffortsManager::getMaxSyllabusTime,
                                    true,
                                    burnMetSource ) ;
        
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
