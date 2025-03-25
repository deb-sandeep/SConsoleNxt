package com.sandy.sconsole.ui.util;

import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.core.util.DayValue;
import com.sandy.sconsole.core.util.DayValueProvider;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.function.Supplier;

@Slf4j
public class HistoricDayValueChart {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd");
    
    private JFreeChart chart = null ;
    private XYPlot plot = null ;
    
    private TimeSeries valueSeries = null ;
    private TimeSeries mavSeries = null ;
    
    private TimeSeriesCollection valDataset = null ;
    private TimeSeriesCollection mavDataset = null ;
    
    private final String  valueAxisLabel ;
    private final Color   valueColor ;
    private final Color   trendColor ;
    private final boolean subtle ;
    
    private final DayValueProvider dataProvider ;
    
    private final Object lock = new Object() ;
    
    private final Supplier<Double> rangeMaxValueSupplier ;
    
    public HistoricDayValueChart( String valueAxisLabel,
                                  Color valueColor,
                                  Color trendColor,
                                  DayValueProvider dataProvider,
                                  Supplier<Double> rangeMaxValueSupplier,
                                  boolean subtle ) {
        
        this.valueAxisLabel = valueAxisLabel ;
        this.valueColor     = valueColor ;
        this.trendColor     = trendColor ;
        this.dataProvider   = dataProvider ;
        this.subtle         = subtle ;
        this.rangeMaxValueSupplier = rangeMaxValueSupplier ;
        
        createDataSet() ;
        createJFreeChart() ;
        refreshChart() ;
    }
    
    private void createDataSet() {
        
        valueSeries = new TimeSeries( "Value" ) ;
        mavSeries = new TimeSeries( "MAV" ) ;
        
        mavSeries = MovingAverage.createMovingAverage( valueSeries,
                                                       "Moving average", 
                                                       7, 0 ) ;
        valDataset = new TimeSeriesCollection() ;
        valDataset.addSeries( valueSeries ) ;
        
        mavDataset = new TimeSeriesCollection() ;
        mavDataset.addSeries( mavSeries ) ;
    }
    
    private void createJFreeChart() {
        
        chart = ChartFactory.createTimeSeriesChart( 
                null,            // Chart title
                null,            // Domain axis label
                valueAxisLabel,  // Range axis label
                mavDataset,      // The dataset
                false,           // Legend required?
                false,           // Tooltips required?
                false            // Does chart generate URLs?
        ) ;
        
        plot = ( XYPlot )chart.getPlot() ;
        plot.setDataset( 1, valDataset ) ;
        
        configureChart() ;
        configurePlot() ;
    }
    
    private void configureChart() {
        chart.setBackgroundPaint( UITheme.BG_COLOR ) ;
        chart.removeLegend() ;
    }
    
    private void configurePlot() {
        
        plot.setBackgroundPaint( UITheme.BG_COLOR ) ;
        
        if( subtle ) {
            plot.setRangeGridlinePaint( Color.DARK_GRAY ) ;
            plot.setDomainGridlinesVisible( false ) ;
            plot.setRangeGridlinesVisible( true ) ;
        }
        
        configureDomainAxis( (DateAxis)plot.getDomainAxis() ) ;
        configureRangeAxis( (NumberAxis)plot.getRangeAxis() ) ;
        
        plot.setRangeAxisLocation( AxisLocation.TOP_OR_RIGHT ) ;
        
        configureValSeriesRenderer() ;
        configureMAVSeriesRenderer() ;
    }
    
    private void configurePlotAxes( ValueAxis axis ) {
        
        Color color = subtle ? Color.DARK_GRAY : Color.LIGHT_GRAY ;
        
        axis.setAxisLinePaint( color ) ;
        axis.setTickLabelPaint( color ) ;
        axis.setTickLabelFont( UITheme.BASE_FONT.deriveFont( 15F ) ) ;
        axis.setLabelPaint( color ) ;
        
        if( subtle ) {
            axis.setLabel( null ) ;
        }
    }
    
    private void configureDomainAxis( DateAxis axis ) {
        configurePlotAxes( axis ) ;
        axis.setDateFormatOverride( DATE_FORMAT ) ;
        axis.setTickMarkPosition( DateTickMarkPosition.MIDDLE ) ;
        axis.setTickLabelFont( UITheme.BASE_FONT.deriveFont( 12F ) ) ;
    }
    
    private void configureRangeAxis( NumberAxis axis ) {
        configurePlotAxes( axis ) ;
    }
    
    private void configureValSeriesRenderer() {

        XYBarRenderer.setDefaultShadowsVisible( false ) ;
        XYBarRenderer renderer = new XYBarRenderer( 0.2F ) ;
        renderer.setDrawBarOutline( false ) ;
        renderer.setSeriesPaint( 0, valueColor ) ;
        renderer.setBarPainter( new StandardXYBarPainter() ) ;
        
        if( subtle ) {
            renderer.setMargin( 0.4F ) ;
        }
        
        plot.setRenderer( 1, renderer ) ;
    }
    
    private void configureMAVSeriesRenderer() {
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer() ;
        renderer.setSeriesPaint( 0, trendColor ) ;
        renderer.setSeriesShape( 0, new Rectangle2D.Double(-2.0, -2.0, 4.0, 4.0) );
        plot.setRenderer( 0, renderer ) ;
    }
    
    public JFreeChart getJFreeChart() {
        return this.chart ;
    }
    
    public void refreshChart() {
        SwingUtilities.invokeLater( () -> {
            try {
                refreshHistoricValuesAsync() ;
            }
            catch( ParseException e ) {
                log.error( "Error populating initial data.", e ) ;
            }
        } ) ;
    }
    
    private void refreshHistoricValuesAsync() 
        throws ParseException {
        
        Collection<DayValue> historicValues ;
        
        synchronized( lock ) {
            valueSeries.clear() ;
            mavSeries.clear() ;

            // Reset the range axis. All the day value charts show the same
            // range which is from 0 to the max of the day values
            if( rangeMaxValueSupplier != null ) {
                NumberAxis rangeAxis = ( NumberAxis ) plot.getRangeAxis() ;
                double maxTimeHrs = rangeMaxValueSupplier.get() ;
                rangeAxis.setRange( 0, Math.ceil( maxTimeHrs ) );
            }
            
            historicValues = dataProvider.getDayValues() ;
            
            if( historicValues != null ) {
                for( DayValue dv : historicValues ) {
                    valueSeries.addOrUpdate( new Day( dv.date() ), dv.value() ) ;
                }
                
                mavSeries = MovingAverage.createMovingAverage( valueSeries,
                                                               "Moving average", 
                                                               7, 0 ) ;
                mavDataset.removeAllSeries() ;
                mavDataset.addSeries( mavSeries ) ;
            }
        }
    }
}
