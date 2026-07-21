package com.sandy.sconsole.ui.util;

import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.core.util.DayValue;
import com.sandy.sconsole.core.util.DayValueProvider;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
public class DayValueChart {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd");
    private static final SimpleDateFormat DAY_KEY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /** Whether a day's burn was fully met (AND'd across whatever topics are
     *  in scope), and if so, whether every contributing topic met it
     *  naturally or at least one only did so via an admin override. */
    public enum BurnMetStatus { UNMET, MET_NATURAL, MET_VIA_OVERRIDE }

    /** Supplies, for the given [start,end] window, the burn-met status of
     *  each day (AND'd across whatever topics are in scope) - absent dates
     *  mean "not applicable" (e.g. nothing was active that day) and should
     *  not be annotated at all. */
    public interface DayBurnMetSource {
        Map<Date, BurnMetStatus> getBurnMetByDate( Date startDate, Date endDate ) ;
    }

    private JFreeChart chart = null ;
    private XYPlot plot = null ;

    private TimeSeries valueSeries = null ;
    private TimeSeries mavSeries = null ;

    private TimeSeriesCollection valDataset = null ;
    private TimeSeriesCollection mavDataset = null ;

    private XYBarRenderer valueRenderer = null ;

    private final String  valueAxisLabel ;
    private final Color   valueColor ;
    private final Color   trendColor ;
    private final boolean subtle ;

    private final DayValueProvider dayValueProvider;
    private final DayBurnMetSource burnMetSource ;

    private final Object lock = new Object() ;

    private final Supplier<Double> rangeMaxValueSupplier ;

    public DayValueChart( String valueAxisLabel,
                          Color valueColor,
                          Color trendColor,
                          DayValueProvider dayValueProvider,
                          Supplier<Double> rangeMaxValueSupplier,
                          boolean subtle ) {
        this( valueAxisLabel, valueColor, trendColor, dayValueProvider,
              rangeMaxValueSupplier, subtle, null ) ;
    }

    public DayValueChart( String valueAxisLabel,
                          Color valueColor,
                          Color trendColor,
                          DayValueProvider dayValueProvider,
                          Supplier<Double> rangeMaxValueSupplier,
                          boolean subtle,
                          DayBurnMetSource burnMetSource ) {

        this.valueAxisLabel   = valueAxisLabel ;
        this.valueColor       = valueColor ;
        this.trendColor       = trendColor ;
        this.dayValueProvider = dayValueProvider ;
        this.subtle           = subtle ;
        this.rangeMaxValueSupplier = rangeMaxValueSupplier ;
        this.burnMetSource    = burnMetSource ;

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
        // Same reasoning as burnMetDataset below - XYLineAndShapeRenderer
        // plots a single point per period with the default START anchor,
        // which lands on the boundary between bars rather than their
        // visual center; MIDDLE aligns the MAV markers with the bars.
        mavDataset.setXPosition( TimePeriodAnchor.MIDDLE ) ;
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
        valueRenderer = new XYBarRenderer( 0.2F ) ;
        valueRenderer.setDrawBarOutline( false ) ;
        valueRenderer.setSeriesPaint( 0, valueColor ) ;
        valueRenderer.setBarPainter( new StandardXYBarPainter() ) ;

        if( subtle ) {
            valueRenderer.setMargin( 0.4F ) ;
        }

        plot.setRenderer( 1, valueRenderer ) ;
    }

    private void configureMAVSeriesRenderer() {

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer() ;
        renderer.setSeriesPaint( 0, trendColor ) ;
        renderer.setSeriesShape( 0, new Rectangle2D.Double(-2.0, -2.0, 4.0, 4.0) );
        plot.setRenderer( 0, renderer ) ;
    }

    private static final Color BURN_MET_COLOR              = new Color( 0, 128, 0 ) ;
    private static final Color BURN_MET_VIA_OVERRIDE_COLOR = new Color( 0, 150, 150 ) ;
    private static final Color BURN_UNMET_COLOR            = new Color( 85, 0, 0 ) ;
    private static final Color BURN_LINE_COLOR             = new Color( 35, 35, 35 ) ;
    private static final long  ONE_DAY_MILLIS              = 24L * 60 * 60 * 1000 ;

    public JFreeChart getJFreeChart() {
        return this.chart ;
    }
    
    public void refreshChart() {
        SwingUtilities.invokeLater( () -> {
            try {
                _refreshHistoricValues() ;
            }
            catch( ParseException e ) {
                log.error( "Error populating initial data.", e ) ;
            }
        } ) ;
    }
    
    private void _refreshHistoricValues()
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
            
            historicValues = dayValueProvider.getDayValues() ;

            if( historicValues != null ) {
                double max = 0 ;
                Date minDate = null, maxDate = null ;
                Map<String, Double> barValueByDayKey = new java.util.HashMap<>() ;

                for( DayValue dv : historicValues ) {
                    valueSeries.addOrUpdate( new Day( dv.date() ), dv.value() ) ;
                    if( dv.value() > max ) {
                        max = dv.value() ;
                    }
                    if( minDate == null || dv.date().before( minDate ) ) minDate = dv.date() ;
                    if( maxDate == null || dv.date().after( maxDate ) )  maxDate = dv.date() ;
                    barValueByDayKey.put( DAY_KEY_FORMAT.format( dv.date() ), dv.value() ) ;
                }

                mavSeries = MovingAverage.createMovingAverage( valueSeries,
                                                               "Moving average",
                                                               7, 0 ) ;
                mavDataset.removeAllSeries() ;
                mavDataset.addSeries( mavSeries ) ;

                // For TotalL60Effort there is no range supplier, hence we
                // force the range axis not to auto-compute. Auto computation
                // takes the minimum to the least of the day values, painting
                // a misleading skyline view.
                if( rangeMaxValueSupplier == null ) {
                    NumberAxis rangeAxis = ( NumberAxis ) plot.getRangeAxis() ;
                    double upper = Math.ceil( max ) ;
                    upper = upper <= 0 ? 9 : upper ;
                    rangeAxis.setRange( 0, upper );
                }

                if( burnMetSource != null && minDate != null ) {
                    refreshBurnMetAnnotations( minDate, maxDate, barValueByDayKey ) ;
                }
            }
        }
    }

    /**
     * Draws a small square above each day's bar - green if that day's burn
     * was fully met naturally, cyan if it was only fully met because an
     * admin override kicked in for at least one topic, red otherwise -
     * joined to the bar top by a thin line just a shade lighter than the
     * chart background. Days absent from the burn-met source (nothing was
     * active/required that day) are left unannotated entirely, rather than
     * drawn as a false "not met".
     */
    private void refreshBurnMetAnnotations( Date minDate, Date maxDate,
                                            Map<String, Double> barValueByDayKey ) {

        plot.clearAnnotations() ;

        NumberAxis rangeAxis = ( NumberAxis ) plot.getRangeAxis() ;
        double axisMax = rangeAxis.getRange().getUpperBound() ;
        // All squares sit at the same Y - the top of the chart - regardless
        // of each day's bar height, so they read as a clean row above the bars.
        double squareY = axisMax ;
        double squareHeight = axisMax * 0.05 ;

        // Match the bar's own rendered width (a day's span, trimmed by
        // whatever margin XYBarRenderer is using) rather than a fixed size,
        // so the square lines up edge-to-edge with the bar underneath it.
        double barWidthMillis = ONE_DAY_MILLIS * ( 1 - valueRenderer.getMargin() ) ;

        Map<Date, BurnMetStatus> burnMetByDate = burnMetSource.getBurnMetByDate( minDate, maxDate ) ;

        for( Map.Entry<Date, BurnMetStatus> entry : burnMetByDate.entrySet() ) {
            Double barValue = barValueByDayKey.get( DAY_KEY_FORMAT.format( entry.getKey() ) ) ;
            if( barValue == null ) continue ;

            Day day = new Day( entry.getKey() ) ;
            double x = day.getMiddleMillisecond() ;
            Color color = switch( entry.getValue() ) {
                case MET_NATURAL      -> BURN_MET_COLOR ;
                case MET_VIA_OVERRIDE -> BURN_MET_VIA_OVERRIDE_COLOR ;
                case UNMET            -> BURN_UNMET_COLOR ;
            } ;

            plot.addAnnotation( new XYLineAnnotation( x, barValue, x, squareY,
                    new BasicStroke( 1.0F ), BURN_LINE_COLOR ) ) ;

            Rectangle2D square = new Rectangle2D.Double(
                    x - barWidthMillis / 2.0, squareY - squareHeight,
                    barWidthMillis, squareHeight ) ;
            plot.addAnnotation( new XYShapeAnnotation( square, null, null, color ) ) ;
        }
    }
}
