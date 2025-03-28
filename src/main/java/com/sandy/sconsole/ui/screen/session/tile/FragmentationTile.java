package com.sandy.sconsole.ui.screen.session.tile;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.core.ui.uiutil.SwingUtils;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.dao.session.dto.SessionDTO;
import com.sandy.sconsole.dao.session.dto.SessionPauseDTO;
import com.sandy.sconsole.dao.session.repo.SessionPauseRepo;
import com.sandy.sconsole.dao.session.repo.SessionRepo;
import com.sandy.sconsole.ui.util.ConfiguredUIAttributes;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jfree.data.time.Day;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@Scope( "prototype" )
public class FragmentationTile extends Tile {
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "E" ) ;
    
    private static final Insets I = new Insets( 0, 0, 0, 1 ) ;
    private static final int DAY_START_HR = 6 ;
    private static final int NUM_HRS_IN_DAY = 24 - DAY_START_HR ;
            
    private final Multimap<Day, SessionDTO>      daySessionsMap = ArrayListMultimap.create() ;
    private final Multimap<Day, SessionPauseDTO> dayPausesMap   = ArrayListMultimap.create() ;
    private final List<Day>                      dayList        = new ArrayList<>() ;
    
    private float dayWidth = 0 ;
    private float hourHeight = 0 ;
    
    private int chartWidth = 0 ;
    private int chartHeight = 0 ;
    
    @Setter private String  syllabusName ;
    @Setter private boolean showDayName = false ;
    
    @Autowired private SessionRepo sessionRepo ;
    @Autowired private SessionPauseRepo sessionPauseRepo ;
    @Autowired private ConfiguredUIAttributes uiAttributes ;
    
    public FragmentationTile() {}
    
    @Override
    public void beforeActivation() {
        daySessionsMap.clear();
        dayPausesMap.clear();
        dayList.clear();
        
        refreshDayList() ;
        sessionRepo.getL30Sessions().forEach(session -> {
            SessionDTO sessionDTO = new SessionDTO( session ) ;
            daySessionsMap.put( new Day( sessionDTO.getStartTime() ), sessionDTO ) ;
        }) ;
        
        sessionPauseRepo.getL30SessionPauses().forEach(sessionPause -> {
            SessionPauseDTO pauseDTO = new SessionPauseDTO( sessionPause ) ;
            dayPausesMap.put( new Day( sessionPause.getStartTime() ), pauseDTO ) ;
        }) ;
    }
    
    private void refreshDayList() {
        Day day = new Day( new Date() ) ;
        dayList.clear() ;
        dayList.add( day ) ;
        for( int i=0; i<30; i++ ) {
            day = (Day)day.previous() ;
            dayList.add( 0, day ) ;
        }
    }
    
    @Override
    public void beforeDeactivation() {
        daySessionsMap.clear();
        dayPausesMap.clear();
        dayList.clear();
    }
    
    @Override
    public void paint( Graphics g ) {
        super.paint( g ) ;
        if( daySessionsMap != null && !daySessionsMap.isEmpty() ) {
            Graphics2D g2D = ( Graphics2D )g ;
            initMeasures() ;
            drawGrid( g2D ) ;
            paintDaySummaries( g2D ) ;
        }
    }
    
    private void initMeasures() {
        
        int dayNameHeight = 0 ;
        
        if( this.showDayName ) {
            dayNameHeight = 15 ;
        }
        
        chartWidth  = getWidth() - I.left - I.right ;
        chartHeight = getHeight() - I.top - I.bottom - dayNameHeight ;
        
        dayWidth = ((float)chartWidth) / dayList.size() ;
        hourHeight = ((float)chartHeight) / NUM_HRS_IN_DAY ;
    }
    
    private void drawGrid( Graphics2D g ) {
        
        Color gridColor = SwingUtils.darkerColor( Color.DARK_GRAY, 0.45F ) ;
        
        g.setColor( Color.DARK_GRAY.darker() ) ;
        g.setFont( UITheme.BASE_FONT ) ;
        g.drawRect( I.left, I.top, chartWidth, chartHeight ) ;
        
        for( int d=0; d<dayList.size(); d++ ) {
            int x = (int)( I.left + dayWidth*d ) ;
            g.setColor( gridColor ) ;
            g.drawLine( x, I.top, x, I.top+chartHeight ) ;
            
            if( this.showDayName ) {
                Day day = dayList.get( d ) ;
                String dayName = SDF.format( day.getStart() ) ;
                
                if( dayName.equals( "Sat" ) || dayName.equals( "Sun" ) ) {
                    g.setColor( Color.DARK_GRAY.brighter() ) ;
                }
                else {
                    g.setColor( Color.DARK_GRAY.darker() ) ;
                }
                g.drawString( dayName, x, I.top + chartHeight + 20 );
            }
        }
        
        for( int h=1; h<NUM_HRS_IN_DAY; h++ ) {
            int hr = DAY_START_HR + h ;
            int y = (int)( I.top + hourHeight*h ) ;
            
            if( hr%6 == 0 ) {
                g.setColor( Color.DARK_GRAY ) ;
            }
            else if( hr%3 == 0 ) {
                g.setColor( Color.DARK_GRAY.darker() ) ;
            }
            else {
                g.setColor( gridColor ) ;
            }
            g.drawLine( I.left, y, I.left+chartWidth, y ) ;
        }
    }
    
    private void paintDaySummaries( Graphics2D g ) {
        
        Color pauseColor = SwingUtils.darkerColor( Color.RED, 0.4F ) ;
        
        for( int dayNum=0; dayNum<dayList.size(); dayNum++ ) {
            Day day = dayList.get( dayNum ) ;
            Collection<SessionDTO> daySessions = daySessionsMap.get( day ) ;
            Collection<SessionPauseDTO> dayPauses = dayPausesMap.get( day ) ;
            
            long firstMil = day.getFirstMillisecond() + DAY_START_HR * 3600 * 1000 ;
            long lastMil  = day.getLastMillisecond() ;
            
            for( SessionDTO session : daySessions ) {
                Color syllabusColor = uiAttributes.getSyllabusColor( session.getSyllabusName() ).darker() ;
                Color fillColor = Color.DARK_GRAY.darker() ;
                if( this.syllabusName == null ) {
                    fillColor = syllabusColor ;
                }
                else if( session.getSyllabusName().equals( this.syllabusName ) ) {
                    fillColor = syllabusColor ;
                }
                paintEvent( dayNum, firstMil, lastMil, session.getStartTime(), session.getDuration(), fillColor, g ) ;
            }
            
            for( SessionPauseDTO pause : dayPauses ) {
                paintEvent( dayNum, firstMil, lastMil, pause.getStartTime(), pause.getDuration(), pauseColor, g ) ;
            }
        }
    }
    
    private void paintEvent( int dayNum, long dayFirstMs, long dayLastMs,
                             Date startTime, int duration, Color fillColor,
                             Graphics2D g ) {
        
        long eventStartMs = startTime.getTime() ;
        
        if( (eventStartMs + duration) < dayFirstMs ) {
            // The event happened between 12 am to 6 am. The fragmentation
            // chart shows time from 0600 to 2359 Hrs. So ignore this
            // event. This is highly unlikely and not worth showing on the chart.
            return ;
        }
        
        if( eventStartMs < dayFirstMs ) {
            duration -= ( int )(( dayFirstMs - eventStartMs )/1000);
            eventStartMs = dayFirstMs ;
        }
        
        if( (eventStartMs + duration * 1000L ) > dayLastMs ) {
            duration = ( int )(( dayLastMs - eventStartMs )/1000);
        }
        
        int x = (int)(I.left + dayNum*dayWidth) ;
        float startHr = (float)( eventStartMs - dayFirstMs )/(1000*60*60) ;
        
        if( startHr > 0 ) {
            int startY = I.top + (int)( startHr * hourHeight ) ;
            int height = (int)( ((float)duration/3600) * hourHeight ) ;
            
            height = Math.max( height, 1 ) ;
            
            g.setColor( fillColor ) ;
            g.fillRect( x, startY, (int)dayWidth, height ) ;
        }
    }
    
    public void highlightSubject( String subjectName ) {
        this.syllabusName = subjectName ;
        repaint() ;
    }
}
