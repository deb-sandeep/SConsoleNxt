package com.sandy.sconsole.ui.screen.session.tile;

import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.bus.EventTargetMarker;
import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.state.ActiveTopicStatistics;
import com.sandy.sconsole.state.manager.ActiveTopicStatisticsManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ThermometerPlot;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.ui.RectangleInsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;

import static com.sandy.sconsole.EventCatalog.ATS_REFRESHED;

@Slf4j
@Component
@Scope( "prototype" )
public class ThermometerTile extends Tile
    implements EventSubscriber {
    
    @Autowired private ActiveTopicStatisticsManager atsManager ;
    @Autowired private EventBus eventBus ;
    
    private ActiveTopicStatistics ats = null ;
    
    private final DefaultValueDataset valueDataset ;
    private final ThermometerPlot plot ;
    private final JFreeChart chart ;

    public ThermometerTile() {
        valueDataset = new DefaultValueDataset( 0 ) ;
        plot = new ThermometerPlot( valueDataset ) ;
        chart = new JFreeChart( plot ) ;
        
        configureChart() ;
        configurePlot() ;
        
        ChartPanel chartPanel = new ChartPanel( chart ) ;
        chartPanel.setMinimumDrawHeight( UITheme.GRID_HEIGHT*14 ) ;
        chartPanel.setMinimumDrawWidth( UITheme.GRID_WIDTH ) ;
        
        add( chartPanel ) ;
    }
    
    private void configureChart() {
        
        chart.setBackgroundPaint( UITheme.BG_COLOR ) ;
        chart.removeLegend() ;
    }
    
    private void configurePlot() {

        plot.setGap( 7 ) ;
        plot.setInsets( new RectangleInsets( -1, -1, -1, -1 ) ) ;
        plot.setPadding(new RectangleInsets( 15.0, 5.0, 15.0, 5.0 ) ) ;
        plot.setBackgroundPaint( UITheme.BG_COLOR ) ;
        plot.setThermometerStroke( new BasicStroke( 1.0f ) ) ;
        plot.setThermometerPaint( Color.GRAY ) ;
        plot.setMercuryPaint( Color.DARK_GRAY ) ;
        plot.setUnits( ThermometerPlot.UNITS_NONE ) ;
        
        plot.setBulbRadius( 40 ) ;
        plot.setColumnRadius( 20 ) ;
        
        plot.setValueLocation( ThermometerPlot.BULB );
        plot.setValuePaint( Color.WHITE ) ;
        plot.setValueFont( UITheme.BASE_FONT.deriveFont( 30F ) ) ;
        
        plot.getRangeAxis().setTickLabelPaint( Color.GRAY ) ;
        plot.getRangeAxis().setTickLabelFont( UITheme.BASE_FONT.deriveFont( 15F ) ) ;
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
        refreshPlot() ;
    }
    
    @Override
    public void beforeDeactivation() {
        eventBus.removeSubscriber( this ) ;
    }
    
    @Override
    public synchronized void handleEvent( Event event ) {
        if( event.getEventId() == ATS_REFRESHED ) {
            refreshPlot() ;
        }
    }
    
    @EventTargetMarker( ATS_REFRESHED )
    private void refreshPlot() {
        
        int maxValue ;
        int targetValue ;
        int curVal ;

        maxValue = NumberUtils.max( new int[]{
                ats.getRequiredBurnRate(),
                ats.getCurrentBurnRate(),
                ats.getNumProblemsSolvedToday()
        } ) + 2 ;
           
        curVal = ats.getNumProblemsSolvedToday() ;
        targetValue = ats.getRequiredBurnRate() ;
        
        // If completion milestone date has passed, revised milestone burn
        // rate has no meaning and will be zero. In this case, set the
        // amber threshold to 0 and green threshold to the max value. This
        // will ensure that the bar will always be in red.
        if( ats.getCurrentZone() == ActiveTopicStatistics.Zone.POST_END ) {
            targetValue = maxValue ;
        }
        
        plot.setRange( 0, maxValue ) ;
        
        plot.setSubrange( 0, 0, targetValue-1 ) ;
        plot.setSubrangePaint( 0, Color.RED.darker() ) ;
        
        plot.setSubrange( 1, targetValue, maxValue ) ;
        plot.setSubrangePaint( 1, Color.GREEN.darker() ) ;
        
        if( curVal > 0 ) {
            plot.setMercuryPaint( Color.GREEN.darker() ) ;
        }
        
        valueDataset.setValue( curVal ) ;
    }
}
