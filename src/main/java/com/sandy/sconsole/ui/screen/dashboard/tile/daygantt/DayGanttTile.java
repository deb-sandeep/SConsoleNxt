package com.sandy.sconsole.ui.screen.dashboard.tile.daygantt;

import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.state.TodayStudyStatistics;
import com.sandy.sconsole.ui.util.ConfiguredUIAttributes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Calendar;
import java.util.Date;

import static com.sandy.sconsole.EventCatalog.TODAY_STUDY_STATS_UPDATED;
import static com.sandy.sconsole.EventCatalog.TODAY_STUDY_TIME_UPDATED;
import static com.sandy.sconsole.core.util.StringUtil.getElapsedTimeLabelHHmm;

/**
 * - Paints the sessions and pauses for the current day in a 24 hour gantt chart
 * - Shows the total effective duration of study for today
 *    - If the time is greater than 5 hours, shows in green, else red
 * - Truncates any sessions and pauses at the day start and end markers
 *   adjusting the effective time accordingly
 *
 * This tile relies on the cached state provided by TodayStudyStatistics
 * for rendering. It also subscribes to events emitted by this class for
 * reactive behavior.
 *
 * <p>
 * Reactive features:
 * ------------------
 * - Lifecycle events:
 *    - Detaches itself from event bus while deactivation
 *    - Reattaches itself to event sources during activation and refreshes
 *      itself before being put to display
 */
@Slf4j
@Component
@Scope( "prototype" )
public class DayGanttTile extends Tile
    implements EventSubscriber {
    
    private static final int[] SUBSCRIBED_EVENTS = {
            TODAY_STUDY_STATS_UPDATED,
            TODAY_STUDY_TIME_UPDATED
    } ;
    
    private static final Insets INSET = new Insets( 0, 0, 25, 0 ) ;
    private static final Font   TOTAL_TIME_FONT = UITheme.BASE_FONT.deriveFont( 30F ) ;
    
    // The expected minimum study hours per day. If total time is less than
    // this, time will show up in red, else green.
    private static final int MIN_TOTAL_TIME_SECONDS = 5*3600 ;
    
    @Autowired private EventBus eventBus ;
    @Autowired private ConfiguredUIAttributes uiAttributes ;
    @Autowired private TodayStudyStatistics studyStats ;
    
    private Dimension tileSize ; // Computed during each paint
    private final Rectangle chartArea = new Rectangle() ; // Populated during each paint
    
    private float numPixelsPerHour = 0 ; // Computed during each paint
    private float numPixelsPerSecond = 0 ; // Computed during each paint
    
    public DayGanttTile() {
        setDoubleBuffered( true ) ;
        setPreferredSize( new Dimension( 10, 10 ) );
    }
    
    @Override
    public void beforeActivation() {
        eventBus.addSubscriberForEventTypes( this, true, SUBSCRIBED_EVENTS) ;
    }
    
    @Override
    public void beforeDeactivation() {
        eventBus.removeSubscriber( this, SUBSCRIBED_EVENTS );
    }
    
    @Override
    public void handleEvent( Event event ) {
        switch( event.getEventType() ) {
            case TODAY_STUDY_STATS_UPDATED:
            case TODAY_STUDY_TIME_UPDATED:
                super.repaint() ;
                break ;
        }
    }
    
    @Override
    public void paint( Graphics gOld ) {
        
        super.paint( gOld ) ;
        Graphics2D g = ( Graphics2D )gOld ;
        
        initPaintingControlUnits() ;
        
        paintBackground( g ) ;
        paintSwimlane( g ) ;
        paintSessions( g ) ;
        paintPauses( g ) ;
        paintTotalTime( g ) ;
    }
    
    private void initPaintingControlUnits() {
        tileSize = getSize() ;
        
        chartArea.x = INSET.left ;
        chartArea.y = INSET.top ;
        chartArea.width = tileSize.width - INSET.left - INSET.right ;
        chartArea.height = tileSize.height - INSET.top - INSET.bottom ;
        
        numPixelsPerHour = ( float )chartArea.width / 24 ; // 24-hour day
        numPixelsPerSecond = numPixelsPerHour/3600 ;
    }
    
    private void paintBackground( Graphics2D g ) {
        g.setColor( UITheme.BG_COLOR ) ;
        g.fillRect( 0, 0, tileSize.width, tileSize.height ) ;
    }
    
    private void paintSwimlane( Graphics2D g ) {
        
        g.setFont( UITheme.BASE_FONT.deriveFont( 15F ) ) ;
        g.setColor( Color.DARK_GRAY.darker() ) ;
        
        g.drawRect( chartArea.x, chartArea.y, chartArea.width, chartArea.height ) ;
        
        for( int i=0; i<24; i++ ) { // 24-hour day
            int x  = (int)(chartArea.x + numPixelsPerHour*i) ;
            int y1 = chartArea.y ;
            int y2 = chartArea.y + chartArea.height ;
            
            g.setColor( Color.DARK_GRAY ) ;
            g.drawLine( x, y1, x, y2 ) ;
            g.drawString( String.valueOf(i), x+5, y2+INSET.bottom-6 ) ;
        }
    }
    
    private void paintSessions( Graphics2D g ) {
        studyStats.getSessions().forEach( session -> {
            Color sessionColor = uiAttributes.getSyllabusColor( session.getSyllabusName() ) ;
            paintArea( session.getStartTime(), session.getDuration(), g, sessionColor ) ;
        } );
    }
    
    private void paintPauses( Graphics2D g ) {
        studyStats.getPauses().forEach( pause -> {
            paintArea( pause.getStartTime(), pause.getDuration(), g, Color.DARK_GRAY ) ;
        } );
    }
    
    private void paintTotalTime( Graphics2D g ) {
        
        // These values are empirically calculated to show the total time
        // in the wee hours of the night where the probability of overlapping
        // a study session is negligible.
        Rectangle area = paintArea( 2, 10, 0, (3600*2 - 20*60), g, UITheme.BG_COLOR ) ;
        
        g.setColor( studyStats.getTotalTimeInSec() >= MIN_TOTAL_TIME_SECONDS ? Color.GREEN : Color.RED ) ;
        g.setFont( TOTAL_TIME_FONT ) ;
        
        FontMetrics metrics = g.getFontMetrics( TOTAL_TIME_FONT ) ;
        int textHeight = metrics.getHeight() ;
        
        g.drawString( getElapsedTimeLabelHHmm( studyStats.getTotalTimeInSec() ),
                      area.x+10,
                      area.y+(area.height/2)+(textHeight/2) - 7 ) ;
    }
    
    private void paintArea( Date startTime, int durationSec, Graphics2D g, Color color ) {
        
        Calendar calendar = Calendar.getInstance() ;
        calendar.setTime( startTime ) ;
        int hours = calendar.get(Calendar.HOUR_OF_DAY) ;
        int minutes = calendar.get(Calendar.MINUTE) ;
        int seconds = calendar.get(Calendar.SECOND) ;
        
        paintArea( hours, minutes, seconds, durationSec, g, color );
    }
    
    private Rectangle paintArea( int startHr, int startMin, int startSec, int durationSec,
                                 Graphics2D g, Color color ) {
        
        Color oldColor = g.getColor() ;
        Rectangle area = getPaintArea( startHr, startMin, startSec, durationSec ) ;
        g.setColor( color ) ;
        g.fillRect( area.x, area.y, area.width, area.height ) ;
        g.setColor( oldColor ) ;
        return area ;
    }
    
    private Rectangle getPaintArea( int startHr, int startMin, int startSec, int durationSec ) {
        
        int start = startHr*3600 + startMin*60 + startSec ;

        Rectangle area = new Rectangle() ;
        area.x = chartArea.x + (int)(start * numPixelsPerSecond) ;
        area.y = chartArea.y + 1 ;
        area.width = (int)(durationSec * numPixelsPerSecond) ;
        area.height = chartArea.height - 1 ;
        
        // If our chart area resolution is less than 1 pixel, we show a minimum of 1 pixel.
        area.width = area.width == 0 ? 1 : area.width ;
        
        return area ;
    }
}
