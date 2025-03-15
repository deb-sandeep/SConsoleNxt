package com.sandy.sconsole.ui.tile.daygantt;

import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.core.ui.uiutil.UIConstant;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.core.util.Day;
import com.sandy.sconsole.dao.session.dto.SessionDTO;
import com.sandy.sconsole.dao.session.dto.SessionPauseDTO;
import com.sandy.sconsole.dao.session.repo.SessionPauseRepo;
import com.sandy.sconsole.dao.session.repo.SessionRepo;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.sandy.sconsole.EventCatalog.*;
import static com.sandy.sconsole.core.util.StringUtil.getElapsedTimeLabelHHmm;

@Slf4j
@Component
@Scope( "prototype" )
public class DayGanttTile extends Tile
    implements EventSubscriber {
    
    private static final Insets INSET           = new Insets( 0, 0, 25, 0 ) ;
    private static final Font   TOTAL_TIME_FONT = UIConstant.BASE_FONT.deriveFont( 30F ) ;
    
    // The expected minimum study hours per day. It total time is less than
    // this, time will show up in red, else green.
    private static final int MIN_TOTAL_TIME_SECONDS = 5*3600 ;
    
    @Autowired private UITheme  theme ;
    @Autowired private EventBus eventBus ;
    
    @Autowired private SessionRepo      sessionRepo ;
    @Autowired private SessionPauseRepo sessionPauseRepo ;

    private Dimension tileSize ;
    private final Rectangle chartArea = new Rectangle() ;
    
    private float numPixelsPerHour = 0 ;
    private float numPixelsPerSecond = 0 ;
    
    // Functional state. These need to be reset in initializeFunctionalState method
    private final Map<Integer, SessionDTO>      todaySessions  = new LinkedHashMap<>() ;
    private final Map<Integer, SessionPauseDTO> todayPauses    = new LinkedHashMap<>() ;
    
    private Day today = new Day() ;
    private int totalTimeInSec = 0 ;
    
    public DayGanttTile() {
        setDoubleBuffered( true ) ;
        setPreferredSize( new Dimension( 10, 10 ) );
    }
    
    @Override
    public void initialize() {
        eventBus.addSubscriberForEventTypes( this, true,
                SESSION_STARTED,
                PAUSE_STARTED,
                SESSION_EXTENDED,
                PAUSE_EXTENDED ) ;
        initializeFunctionalState() ;
    }
    
    @Override
    public void paint( Graphics gOld ) {
        super.paint( gOld ) ;
        Graphics2D g = ( Graphics2D )gOld ;
        
        tileSize = getSize() ;
        initPaintingControlUnits() ;
        
        paintBackground( g ) ;
        paintSwimlane( g ) ;
        paintTotalTime( g ) ;
    }
    
    private void initPaintingControlUnits() {
        
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
    
    private void paintTotalTime( Graphics2D g ) {
        
        // These values are empirically calculated to show the total time
        // in the wee hours of the night where the probability of overlapping
        // a study session is negligible.
        int startSec = 3600*2 + 10*60 ;
        int duration = 3600*2 - 20*60 ;
        
        int x1 = chartArea.x + (int)(startSec * numPixelsPerSecond) ;
        int y1 = chartArea.y + 1 ;
        int width = (int)(duration * numPixelsPerSecond) ;
        int height = chartArea.height - 1 ;
        
        g.setColor( theme.getBackgroundColor() ) ;
        g.fillRect( x1, y1, width, height ) ;
        g.setColor( totalTimeInSec >= MIN_TOTAL_TIME_SECONDS ? Color.GREEN : Color.RED ) ;
        g.setFont( TOTAL_TIME_FONT ) ;
        
        FontMetrics metrics = g.getFontMetrics( TOTAL_TIME_FONT ) ;
        int textHeight = metrics.getHeight() ;
        
        g.drawString( getElapsedTimeLabelHHmm( totalTimeInSec ),
                      x1+10, 
                      y1+(height/2)+(textHeight/2) - 7 ) ;
    }
    
    private void initializeFunctionalState() {
        
        // Clear any existing state
        today = new Day() ;
        todaySessions.clear() ;
        todayPauses.clear() ;
        totalTimeInSec = 0 ;
        
        // Load the existing sessions and pauses for today
        sessionRepo.getTodaySessions()
                .forEach( session -> updateSession( new SessionDTO( session ) ) ) ;
        sessionPauseRepo.getTodayPauses()
                .forEach( pause -> updatePause( new SessionPauseDTO( pause ) ) ) ;
        
        super.repaint() ;
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
