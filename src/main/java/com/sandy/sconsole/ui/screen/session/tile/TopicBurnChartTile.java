package com.sandy.sconsole.ui.screen.session.tile;

import com.sandy.sconsole.EventCatalog;
import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.dao.session.repo.ProblemAttemptRepo;
import com.sandy.sconsole.state.ActiveTopicStatistics;
import com.sandy.sconsole.state.manager.ActiveTopicStatisticsManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.statistics.Regression;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@Scope( "prototype" )
public class TopicBurnChartTile extends Tile
    implements EventSubscriber {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/M");

    private static final int[] SUBSCRIBED_EVENTS = {
            EventCatalog.ATS_REFRESHED,
            EventCatalog.SESSION_ENDED
    } ;
    
    @Autowired private ActiveTopicStatisticsManager atsManager ;
    @Autowired private EventBus eventBus ;

    // Instance variables are arranged in the order of initialization
    private ActiveTopicStatistics ats = null ;
    
    private TimeSeries historicBurn = null ;
    private TimeSeries           baseBurnProjection     = null ;
    private TimeSeries           historicBurnRegression = null ;
    private TimeSeriesCollection seriesColl             = null ;
    
    private JFreeChart chart = null ;
    private XYPlot plot = null ;
    
    private ChartPanel chartPanel = null ;
    
    public TopicBurnChartTile() {
        this.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
    }
    
    public void setTopicId( int topicId ) {
        this.ats = atsManager.getTopicStatistics( topicId ) ;
    }
    
    @Override
    public void beforeActivation() {
        
        if( ats == null ) {
            throw new RuntimeException( "No active topic statistics found" ) ;
        }
        
        // Create, Configure and Attach a fresh chart to the tile
        // If an old chart exits, safely deconstruct it before attaching the new one
        configureTimeSeries() ;
        configureChart() ;
        configurePlot() ;
        configureRenderer() ;
        configureAxes() ;
        attachChartPanel() ;
        scheduleReplot() ;
        
        eventBus.addSubscriberForEventTypes( this, true, SUBSCRIBED_EVENTS ) ;
    }
    
    @Override
    public void beforeDeactivation() {
        eventBus.removeSubscriber( this ) ;
    }
    
    private void configureTimeSeries() {
        
        historicBurn = new TimeSeries( "Historic daily burn" ) ;
        baseBurnProjection = new TimeSeries( "Base burn projection" ) ;
        historicBurnRegression = new TimeSeries( "Projected burn - velocity" ) ;
        
        seriesColl = new TimeSeriesCollection() ;
        seriesColl.addSeries( historicBurn ) ;
        seriesColl.addSeries( baseBurnProjection ) ;
        seriesColl.addSeries( historicBurnRegression ) ;
    }
    
    private void configureChart() {
        
        chart = ChartFactory.createTimeSeriesChart( null, null, null, seriesColl ) ;
        chart.setBackgroundPaint( UITheme.BG_COLOR ) ;
        chart.removeLegend() ;
    }
    
    private void configurePlot() {
        
        plot = ( XYPlot )chart.getPlot() ;
        plot.setBackgroundPaint( UITheme.BG_COLOR ) ;
        plot.setDomainGridlinePaint( Color.DARK_GRAY ) ;
        plot.setRangeGridlinePaint( Color.DARK_GRAY ) ;
        plot.setRangePannable( false ) ;
        plot.setDomainPannable( false ) ;
        plot.setDomainGridlinesVisible( true ) ;
        plot.setRangeGridlinesVisible( true ) ;
    }
    
    private void configureRenderer() {
        
        AbstractRenderer renderer = ( AbstractRenderer )plot.getRenderer() ;
        
        renderer.setSeriesPaint( 0, UITheme.HISTORIC_BURN_COLOR ) ;
        renderer.setSeriesPaint( 1, UITheme.BASE_BURN_COLOR ) ;
        renderer.setSeriesPaint( 2, UITheme.HISTORIC_BURN_REGRESSION_COLOR ) ;
        
        renderer.setSeriesStroke( 0, new BasicStroke(1.0f) );
        
        renderer.setSeriesStroke( 1, new BasicStroke(
                1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1.0f, new float[] {2.0f, 6.0f}, 0.0f) );
        
        renderer.setSeriesStroke( 2, new BasicStroke(
                1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1.0f, new float[] {2.0f, 6.0f}, 0.0f) );
    }
    
    private void configureAxes() {
        
        DateAxis xAxis = (DateAxis) plot.getDomainAxis() ;
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis() ;
        
        xAxis.setLabelFont( UITheme.CHART_XAXIS_FONT ) ;
        xAxis.setTickLabelFont( UITheme.CHART_XAXIS_FONT ) ;
        xAxis.setTickLabelPaint( Color.LIGHT_GRAY.darker() ) ;
        xAxis.setMinorTickMarksVisible( true ) ;
        xAxis.setDateFormatOverride( DATE_FORMAT ) ;
        
        yAxis.setLabelFont( UITheme.CHART_YAXIS_FONT ) ;
        yAxis.setTickLabelFont( UITheme.CHART_YAXIS_FONT ) ;
        yAxis.setTickLabelPaint( Color.LIGHT_GRAY.darker() ) ;
    }
    
    private void attachChartPanel() {
        
        if( chartPanel != null ) {
            remove( chartPanel ) ;
        }
        chartPanel = new ChartPanel( chart ) ;
        chartPanel.setDoubleBuffered( true ) ;
        add( chartPanel ) ;
    }
    
    @Override
    public void handleEvent( Event event ) {
        if( event.getEventType() == EventCatalog.ATS_REFRESHED ) {
            scheduleReplot() ;
        }
    }

    private void scheduleReplot() {
        SwingUtilities.invokeLater( () -> {
            try {
                _redrawBurnChart() ;
            }
            catch( Exception e ) {
                log.debug( "Exception processing topic change.", e ) ;
            }
        } );
    }
    
    // This is asynchronously scheduled via the scheduleReplot method and
    // should not be invoked directly.
    private synchronized void _redrawBurnChart() {
        
        chart.setNotify( false ) ;
        {
            clearChartData() ;
            
            plotMilestoneMarker() ;
            plotBaseMilestoneBurn() ;
            plotHistoricBurns() ;
            plotHistoricBurnRegression() ;
            
            historicBurn.fireSeriesChanged() ;
            baseBurnProjection.fireSeriesChanged() ;
            historicBurnRegression.fireSeriesChanged() ;
        }
        chart.setNotify( true ) ;
    }
    
    private void clearChartData() {
        
        baseBurnProjection.clear() ;
        historicBurn.clear() ;
        historicBurnRegression.clear() ;
        
        plot.clearRangeMarkers() ;
        plot.clearDomainMarkers() ;
    }
    
    private void plotMilestoneMarker() {
        
        Marker startMarker ;
        Marker startBufferEndMarker = null;
        Marker theoryEndMarker = null ;
        Marker exerciseEndMarker ;
        Marker endMarker = null ;
        
        startMarker = new ValueMarker( ats.getStartDate().getTime() ) ;
        exerciseEndMarker = new ValueMarker( ats.getExerciseEndDate().getTime() ) ;
        
        if( ats.getNumStartBufferDays() > 0 ) {
            Date startBufferEndDate = DateUtils.addDays( ats.getStartDate(), ats.getNumStartBufferDays()-1 ) ;
            startBufferEndMarker = new ValueMarker( startBufferEndDate.getTime() ) ;
        }
        
        if( ats.getNumTheoryDays() > 0 ) {
            Date theoryEndDate = DateUtils.addDays( ats.getStartDate(), ats.getNumTheoryDays() + ats.getNumStartBufferDays() - 1 ) ;
            theoryEndMarker = new ValueMarker( theoryEndDate.getTime() ) ;
        }
        
        if( ats.getNumEndBufferDays() > 0 ) {
            Date endDate = ats.getEndDate() ;
            endMarker = new ValueMarker( endDate.getTime() ) ;
        }
        
        Marker[] markers = { startMarker, startBufferEndMarker, theoryEndMarker, exerciseEndMarker, endMarker } ;
        Arrays.stream( markers ).forEach( marker -> {
            if( marker != null ) {
                marker.setPaint( Color.GRAY ) ;
                plot.addDomainMarker( marker ) ;
            }
        }) ;
    }
    
    private void plotBaseMilestoneBurn() {
        
        final int exerciseStartDayNum = ats.getNumStartBufferDays() + ats.getNumTheoryDays() ;
        final int exerciseEndDayNum = exerciseStartDayNum + ats.getNumExerciseDays() ;
        final int topicEndDayNum = ats.getNumTotalDays() ;
        
        int numRemainingProblems = ats.getNumTotalProblems() ;
        
        for( int dayCount = 0; dayCount < topicEndDayNum; dayCount++ ) {
            Day day = new Day( DateUtils.addDays( ats.getStartDate(), dayCount ) ) ;
            
            int plannedDayBurn = 0 ;
            if( dayCount >= exerciseStartDayNum && dayCount < exerciseEndDayNum ) {
                plannedDayBurn = ats.getOriginalBurnRate() ;
            }
            
            numRemainingProblems -= plannedDayBurn ;
            numRemainingProblems = Math.max( numRemainingProblems, 0 ) ;
            
            baseBurnProjection.add( day, numRemainingProblems, false ) ;
        }
    }
    
    private void plotHistoricBurns() {
        
        int numRemainingProblems = ats.getNumTotalProblems() ;
        List<ProblemAttemptRepo.DayBurnStat> histBurns = ats.getHistoricBurns() ;
        
        if( !histBurns.isEmpty() ) {
            ProblemAttemptRepo.DayBurnStat firstDayBurnStats = histBurns.get( 0 ) ;
            historicBurn.add( new Day( firstDayBurnStats.getDate() ).previous(), numRemainingProblems ) ;

            for( ProblemAttemptRepo.DayBurnStat hb : ats.getHistoricBurns() ) {
                Date date = hb.getDate() ;
                numRemainingProblems -= hb.getNumQuestionsSolved() ;
                historicBurn.add( new Day( date ), numRemainingProblems, false ) ;
            }
            
            NumberAxis yAxis = ( NumberAxis )plot.getRangeAxis() ;
            yAxis.setRange( 0, (1.05*ats.getNumTotalProblems()) ) ;
        }
    }
    
    private void plotHistoricBurnRegression() {
        
        // If there is no historic burn data or the burn has less than two items,
        // a regression line can't be plotted
        if( historicBurn.getItemCount() < 2 ) return ;
        
        double[] coefficients = Regression.getOLSRegression( this.seriesColl, 0 ) ;
        LineFunction2D line = new LineFunction2D( coefficients[0], coefficients[1] ) ;
        
        Day startDay = (Day)historicBurn.getDataItem( historicBurn.getItemCount()-1 ).getPeriod() ;
        Day endDay = (Day)baseBurnProjection.getDataItem( baseBurnProjection.getItemCount()-1 ).getPeriod() ;
        Day day = startDay ;
        
        while( day.getFirstMillisecond() <= endDay.getFirstMillisecond() ) {
            historicBurnRegression.add( day, line.getValue( day.getFirstMillisecond() ) ) ;
            day = (Day)day.next() ;
        }
    }
}
