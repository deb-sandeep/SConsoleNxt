package com.sandy.sconsole.ui.screen.dashboard.tile.burn;

import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.state.ActiveTopicStatistics;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
@Scope( "prototype" )
class BurnMeterCanvas extends JPanel {
    
    private static final Color GRID_COLOR             = Color.decode( "#343333" ) ;
    private static final Color OVERBURN_COLOR         = Color.decode( "#37CB37" ) ;
    private static final Color SLIGHT_UNDERBURN_COLOR = Color.decode( "#30AF30" ) ;
    private static final Color UNDERBURN_COLOR        = Color.decode( "#2D832D" ) ;
    private static final Color TARGET_BURN_COLOR      = Color.GREEN ;
    public  static final Color CURRENT_BURN_COLOR     = Color.decode( "#BBBABA" ) ;
    private static final Color UNBURNT_COLOR          = Color.decode( "#B10722" ) ;
    
    private static final Font VALUE_FONT = UITheme.BASE_FONT ;
    
    private final Insets BORDER = new Insets( 2, 10, 10, 10 ) ;
    
    @Setter private ActiveTopicStatistics topicStats ;
    
    @Getter private int currentBurnRate  = 0 ;
    @Getter private int requiredBurnRate = 0 ;
    @Getter private int overshootDays = 0 ;
    
    private int maxValue = 0 ;
    private int todayBurn = 0 ;
    
    private int width = 0 ;
    private int height = 0 ;
    private float widthPerProblem = 0 ;
    
    public BurnMeterCanvas() {}
    
    @PostConstruct
    public void init() {
        setBackground( UITheme.BG_COLOR ) ;
    }
    
    void refreshUI() {
        
        maxValue = 0 ;
        currentBurnRate = 0 ;
        requiredBurnRate = 0 ;
        todayBurn = 0 ;
        
        if( topicStats != null ) {
            maxValue = NumberUtils.max( topicStats.getRequiredBurnRate(),
                                        topicStats.getNumProblemsSolvedToday() ) + 2 ;
            
            currentBurnRate = topicStats.getCurrentBurnRate() ;
            requiredBurnRate = topicStats.getRequiredBurnRate() ;
            todayBurn = topicStats.getNumProblemsSolvedToday() ;
            overshootDays = topicStats.getNumOvershootDays() ;
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
            
            fillRequiredBurnRateBar( (Graphics2D)g ) ;
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
        if( currentBurnRate > 0 ) {
            int x = (int)( BORDER.left + currentBurnRate * widthPerProblem ) ;
            g.setColor( CURRENT_BURN_COLOR ) ;
            g.drawRect( x, markerY, markerWidth, markerHeight );
        }
        
        // Paint the target burn marker
        int x = (int)( BORDER.left + requiredBurnRate * widthPerProblem ) ;
        g.setColor( TARGET_BURN_COLOR ) ;
        g.drawRect( x, markerY, markerWidth, markerHeight );
    }
    
    private void fillRequiredBurnRateBar( Graphics2D g ) {
        paintBar( requiredBurnRate, UNBURNT_COLOR, g ) ;
    }
    
    private void paintCurrentValue( Graphics2D g ) {
        
        if( todayBurn >= requiredBurnRate ) {
            paintBar( todayBurn, OVERBURN_COLOR, g ) ;
        }
        else if( todayBurn >= currentBurnRate ) {
            paintBar( todayBurn, SLIGHT_UNDERBURN_COLOR, g ) ;
        }
        else {
            paintBar( todayBurn, UNDERBURN_COLOR.darker(), g ) ;
        }
        
        if( todayBurn > 0 ) {
            paintValue( todayBurn, g ) ;
        }
    }
    
    private void paintBar( int value, Color color, Graphics2D g ) {
        
        int endX = (int)( BORDER.left + value * widthPerProblem ) ;
        
        g.setColor( color ) ;
        g.fillRect( BORDER.left, BORDER.top, (endX-BORDER.left), height ) ;
    }
    
    private void paintValue( int todayBurn, Graphics2D g ) {
        
        FontMetrics metrics = g.getFontMetrics( VALUE_FONT ) ;
        int textHeight = metrics.getAscent() ; // Since we are dealing only with numbers
        int textWidth  = metrics.stringWidth( String.valueOf( todayBurn ) ) ;

        int yPos = BORDER.top + height/2 + textHeight/2 ;
        int xPos = (int)( BORDER.left + todayBurn * widthPerProblem - textWidth - 5 ) ;
        
        g.setColor( Color.WHITE ) ;
        g.setFont( VALUE_FONT );
        g.drawString( String.valueOf( todayBurn ), xPos, yPos ) ;
    }
}
