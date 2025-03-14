package com.sandy.sconsole.ui.tile.daygantt;

import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.core.ui.uiutil.UIConstant;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.text.SimpleDateFormat;

import static com.sandy.sconsole.core.util.StringUtil.getElapsedTimeLabel;

@Slf4j
@Component
@Scope( "prototype" )
public class DayGanttTile extends Tile {
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "yyyy-MM-dd" ) ;
    private static final int START_HR = 0 ;
    private static final int END_HR = 24 ;
    private static final int NUM_HRS = END_HR - START_HR ;
    private static final Insets INSET = new Insets( 0, 0, 25, 0 ) ;
    private static final Font TOTAL_FONT = UIConstant.BASE_FONT.deriveFont( 30F ) ;
    
    @Autowired private UITheme theme ;

    private final Rectangle chartArea = new Rectangle() ;
    
    private float numPixelsPerHour = 0 ;
    private float numPixelsPerSecond = 0 ;
    
    private int totalTimeInSec = 0 ;
    
    public DayGanttTile() {}
    
    @Override
    public void initialize() {
        super.initialize() ;
        setForeground( Color.CYAN ) ;
        setDoubleBuffered( true ) ;
        setPreferredSize( new Dimension( 100, 100 ) );
    }
    
    @Override
    public void paint( Graphics gOld ) {
        super.paint( gOld ) ;
        Graphics2D g = ( Graphics2D )gOld ;
        
        Dimension dim = getSize() ;
        initializeYardsticks( dim ) ;

        g.setColor( theme.getBackgroundColor() ) ;
        g.fillRect( 0, 0, dim.width, dim.height ) ;
        
        paintSwimlane( g ) ;
        refreshTotalTime( g ) ;
    }
    
    private void initializeYardsticks( Dimension dim ) {
        chartArea.x = INSET.left ;
        chartArea.y = INSET.top ;
        chartArea.width = dim.width - INSET.left - INSET.right ;
        chartArea.height = dim.height - INSET.top - INSET.bottom ;
        
        numPixelsPerHour = ( float )chartArea.width / NUM_HRS ;
        numPixelsPerSecond = numPixelsPerHour/3600 ;
        
        totalTimeInSec = 0 ;
    }
    
    private void paintSwimlane( Graphics2D g ) {
        
        g.setFont( UIConstant.BASE_FONT.deriveFont( 15F ) ) ;
        g.setColor( Color.DARK_GRAY.darker() ) ;
        
        g.drawRect( chartArea.x, chartArea.y, chartArea.width, chartArea.height ) ;
        
        for( int i=0; i<NUM_HRS; i++ ) {
            int x  = (int)(chartArea.x + numPixelsPerHour*i) ;
            int y1 = chartArea.y ;
            int y2 = chartArea.y + chartArea.height ;
            
            g.setColor( Color.DARK_GRAY ) ;
            g.drawLine( x, y1, x, y2 ) ;
            g.drawString( ""+(START_HR+i), x+5, y2+INSET.bottom-6 ) ;
        }
    }
    
    private void refreshTotalTime( Graphics2D g ) {
        
        if( g == null )return ;
        
        int startSec = 3600*2 + 10*60 ;
        int duraction = 3600*3 - 20*60 ;
        
        int x1 = chartArea.x + (int)(startSec * numPixelsPerSecond) ;
        int y1 = chartArea.y + 1 ;
        int width = (int)(duraction * numPixelsPerSecond) ;
        int height = chartArea.height-1 ;
        
        g.setColor( theme.getBackgroundColor() ) ;
        g.fillRect( x1, y1, width, height ) ;
        g.setColor( Color.GRAY ) ;
        g.setFont( TOTAL_FONT ) ;
        
        FontMetrics metrics = g.getFontMetrics( TOTAL_FONT ) ;
        int textHeight = metrics.getHeight() ;
        
        g.drawString( getElapsedTimeLabel( totalTimeInSec ), 
                      x1+10, 
                      y1+(height/2)+(textHeight/2)-10 ) ;
    }

}
