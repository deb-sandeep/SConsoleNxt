package com.sandy.sconsole.ui.tile.daygantt;

import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.clock.ClockTickListener;
import com.sandy.sconsole.core.clock.SConsoleClock;
import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.core.ui.uiutil.UIConstant;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.core.util.Day;
import com.sandy.sconsole.dao.session.dto.SessionDTO;
import com.sandy.sconsole.dao.session.dto.SessionPauseDTO;
import com.sandy.sconsole.dao.session.repo.SessionPauseRepo;
import com.sandy.sconsole.dao.session.repo.SessionRepo;
import com.sandy.sconsole.ui.ConfiguredUIAttributes;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.sandy.sconsole.EventCatalog.*;
import static com.sandy.sconsole.core.util.StringUtil.getElapsedTimeLabelHHmm;

@Slf4j
@Component
@Scope( "prototype" )
public class DayGanttTile extends Tile
    implements EventSubscriber, ClockTickListener {
    
    private static final Insets INSET = new Insets( 0, 0, 25, 0 ) ;
    private static final Font   TOTAL_TIME_FONT = UIConstant.BASE_FONT.deriveFont( 30F ) ;
    
    // The expected minimum study hours per day. If total time is less than
    // this, time will show up in red, else green.
    private static final int MIN_TOTAL_TIME_SECONDS = 5*3600 ;
    
    @Autowired private UITheme theme ;
    @Autowired private EventBus eventBus ;
    @Autowired private SConsoleClock clock ;
    @Autowired private SessionRepo sessionRepo ;
    @Autowired private SessionPauseRepo sessionPauseRepo ;
    @Autowired private ConfiguredUIAttributes uiAttributes ;
    
    private Dimension tileSize ; // Computed during each paint
    private final Rectangle chartArea = new Rectangle() ; // Populated during each paint
    
    private float numPixelsPerHour = 0 ; // Computed during each paint
    private float numPixelsPerSecond = 0 ; // Computed during each paint
    
    // Functional state. These need to be reset in initializeFunctionalState method
    private final Map<Integer, SessionDTO>      todaySessions  = new LinkedHashMap<>() ;
    private final Map<Integer, SessionPauseDTO> todayPauses    = new LinkedHashMap<>() ;
    
    private Day today = new Day() ;
    private int totalTimeInSec = 0 ;
    
    private static final int[] SUBSCRIBED_EVENTS = {
            SESSION_STARTED,
            PAUSE_STARTED,
            SESSION_EXTENDED,
            PAUSE_EXTENDED
    } ;
    
    public DayGanttTile() {
        setDoubleBuffered( true ) ;
        setPreferredSize( new Dimension( 10, 10 ) );
    }
    
    @Override
    public void beforeActivation() {
        clock.addTickListener( this, TimeUnit.DAYS ) ;
        eventBus.addSubscriberForEventTypes( this, true, SUBSCRIBED_EVENTS) ;
        initializeFunctionalState() ;
    }
    
    @Override
    public void beforeDeactivation() {
        clock.removeTickListener( this ) ;
        eventBus.removeSubscriber( this, SUBSCRIBED_EVENTS );
    }
    
    @Override
    public void dayTicked( Calendar calendar ) {
        initializeFunctionalState() ;
    }
    
    private void initializeFunctionalState() {
        
        // Clear any existing state
        today = new Day() ;
        todaySessions.clear() ;
        todayPauses.clear() ;
        totalTimeInSec = 0 ;
        
        // Load the existing sessions and pauses for today
        sessionRepo.getTodaySessions()
                .forEach( s -> updateSession( new SessionDTO( s ) ) ) ;
        sessionPauseRepo.getTodayPauses()
                .forEach( p -> updatePause( new SessionPauseDTO( p ) ) ) ;
        
        super.repaint() ;
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
        g.setColor( theme.getBackgroundColor() ) ;
        g.fillRect( 0, 0, tileSize.width, tileSize.height ) ;
    }
    
    private void paintSwimlane( Graphics2D g ) {
        
        g.setFont( UIConstant.BASE_FONT.deriveFont( 15F ) ) ;
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
        todaySessions.values().forEach( session -> {
            log.debug( "paintSessions: {}", session );
            Color sessionColor = uiAttributes.getSyllabusColor( session.getSyllabusName() ) ;
            paintArea( session.getStartTime(), session.getDuration(), g, sessionColor ) ;
        } );
    }
    
    private void paintPauses( Graphics2D g ) {
        todayPauses.values().forEach( pause -> {
            log.debug( "paintPauses: {}", pause );
            paintArea( pause.getStartTime(), pause.getDuration(), g, Color.DARK_GRAY ) ;
        } );
    }
    
    private void paintTotalTime( Graphics2D g ) {
        
        // These values are empirically calculated to show the total time
        // in the wee hours of the night where the probability of overlapping
        // a study session is negligible.
        Rectangle area = paintArea( 2, 10, 0, (3600*2 - 20*60), g, theme.getBackgroundColor() ) ;
        
        g.setColor( totalTimeInSec >= MIN_TOTAL_TIME_SECONDS ? Color.GREEN : Color.RED ) ;
        g.setFont( TOTAL_TIME_FONT ) ;
        
        FontMetrics metrics = g.getFontMetrics( TOTAL_TIME_FONT ) ;
        int textHeight = metrics.getHeight() ;
        
        g.drawString( getElapsedTimeLabelHHmm( totalTimeInSec ),
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
    
    @Override
    public void handleEvent( Event event ) {
        
        switch( event.getEventType() ) {
            case SESSION_STARTED:
            case SESSION_EXTENDED:
                updateSession( ( SessionDTO )event.getValue() ) ;
                break ;
                
            case PAUSE_STARTED:
            case PAUSE_EXTENDED:
                updatePause( ( SessionPauseDTO )event.getValue() ) ;
                break ;
        }
        super.repaint() ;
    }
    
    private void updateSession( @NonNull SessionDTO _session ) {
        
        // Create a new copy. The incoming instance might be a shared
        // instance (e.g. published on event bus)
        SessionDTO session = new SessionDTO( _session ) ;
        
        // This overwrites any existing session with the same id.
        todaySessions.put( session.getId(), session ) ;
        if( today.after( session.getStartTime() ) ) {
            session.setStartTime( today.getStartTime() ) ;
        }
        else if( today.before( session.getEndTime() ) ) {
            session.setEndTime( today.getEndTime() ) ;
        }
        computeTotalEffectiveTimeForToday() ;
    }
    
    private void updatePause( @NonNull SessionPauseDTO _pause ) {
        
        SessionPauseDTO pause = new SessionPauseDTO( _pause ) ;
        todayPauses.put( pause.getId(), pause ) ;
        
        if( today.after( pause.getStartTime() ) ) {
            pause.setStartTime( today.getStartTime() ) ;
        }
        else if( today.before( pause.getEndTime() ) ) {
            pause.setEndTime( today.getEndTime() ) ;
        }
        computeTotalEffectiveTimeForToday() ;
    }
    
    private void computeTotalEffectiveTimeForToday() {
        
        int totalSessionTime = todaySessions.values().stream().mapToInt( SessionDTO::getDuration ).sum() ;
        int totalPauseTime = todayPauses.values().stream().mapToInt( SessionPauseDTO::getDuration ).sum() ;
        
        this.totalTimeInSec = totalSessionTime - totalPauseTime ;
    }
}
