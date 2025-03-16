package com.sandy.sconsole.ui.screen.dashboard.tile.burn;

import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.ui.util.ActiveTopicStatistics;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
@Scope( "prototype" )
class BurnMeterCanvas extends JPanel {
    
    private final Color GRID_COLOR             = Color.decode( "#343333" ) ;
    private final Color OVERBURN_COLOR         = Color.decode( "#386130" ) ;
    private final Color SLIGHT_UNDERBURN_COLOR = Color.decode( "#386130" ) ;
    private final Color UNDERBURN_COLOR        = Color.decode( "#5B2525" ) ;
    private final Color ORIGINAL_BURN_COLOR    = Color.decode( "#7E7D7D" ) ;
    private final Color TARGET_BURN_COLOR      = Color.GREEN ;
    
    private final Insets BORDER = new Insets( 2, 10, 10, 10 ) ;
    
    @Autowired private UITheme theme ;
    
    @Setter private ActiveTopicStatistics topicStats ;
    
    private int maxValue         = 0 ;
    private int originalBurnRate = 0 ;
    private int requiredBurnRate = 0 ;
    private int todayBurn        = 0 ;
    
    private int   width           = 0 ;
    private int   height          = 0 ;
    private float widthPerProblem = 0 ;
    
    public BurnMeterCanvas() {}
    
    @PostConstruct
    public void init() {
        setBackground( theme.getBackgroundColor() ) ;
    }
    
    void refreshUI() {
        
        maxValue = 0 ;
        originalBurnRate = 0 ;
        requiredBurnRate = 0 ;
        todayBurn = 0 ;
        
        if( topicStats != null ) {
            maxValue = NumberUtils.max( new int[]{
                    topicStats.getRequiredBurnRate(),
                    topicStats.getOriginalBurnRate(),
                    topicStats.getNumProblemsSolvedToday()
            } ) + 2 ;
            
            originalBurnRate = topicStats.getOriginalBurnRate() ;
            requiredBurnRate = topicStats.getRequiredBurnRate() ;
            todayBurn = topicStats.getNumProblemsSolvedToday() ;
        }
        super.repaint() ;
    }
    
    @Override
    public void paint( Graphics g ) {
        
        super.paint( g ) ;
        
        // If we don't have a topic, leave the burn meter blank
        if( topicStats != null ) {
            // Recompute the width per problem based on the current component dimensions
            width = getWidth() - BORDER.left - BORDER.right ;
            height = getHeight() - BORDER.top - BORDER.bottom ;
            widthPerProblem = ((float)width)/maxValue ;
            
            paintCurrentValue( (Graphics2D)g ) ;
            paintGrid( (Graphics2D)g ) ;
        }
    }
    
    private void paintGrid( Graphics2D g ) {
        
        // Paint the empty problem grid
        g.setColor( GRID_COLOR ) ;
        g.drawRect( BORDER.left, BORDER.top, width, height ) ;
        for( int i=1; i<=maxValue; i++ ) {
            int x = (int)(BORDER.left + widthPerProblem * i ) ;
            g.drawLine( x, BORDER.top, x, BORDER.top+height ) ;
        }
        
        int markerY = BORDER.top/2 ;
        int markerHeight = height + BORDER.top/2 + BORDER.bottom/2 ;
        int markerWidth = 3 ;
        
        // Paint the original burn rate marker
        if( originalBurnRate > 0 ) {
            int x = (int)( BORDER.left + originalBurnRate * widthPerProblem ) ;
            g.setColor( ORIGINAL_BURN_COLOR ) ;
            g.drawRect( x, markerY, markerWidth, markerHeight );
        }
        
        // Paint the target burn marker
        int x = (int)( BORDER.left + requiredBurnRate * widthPerProblem ) ;
        g.setColor( TARGET_BURN_COLOR ) ;
        g.drawRect( x, markerY, markerWidth, markerHeight );
    }
    
    private void paintCurrentValue( Graphics2D g ) {
        
        if( todayBurn >= requiredBurnRate ) {
            paintValue( todayBurn, OVERBURN_COLOR, g ) ;
        }
        else if( todayBurn >= originalBurnRate ) {
            paintValue( todayBurn, SLIGHT_UNDERBURN_COLOR, g ) ;
        }
        else {
            paintValue( todayBurn, UNDERBURN_COLOR.darker(), g ) ;
        }
    }
    
    private void paintValue( int maxVal, Color color, Graphics2D g ) {
        
        int endX = (int)( BORDER.left + maxVal * widthPerProblem ) ;
        
        g.setColor( color ) ;
        g.fillRect( BORDER.left, BORDER.top, (endX-BORDER.left), height ) ;
    }
}
