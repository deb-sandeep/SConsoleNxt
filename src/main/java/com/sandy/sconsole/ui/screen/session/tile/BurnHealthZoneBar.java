package com.sandy.sconsole.ui.screen.session.tile;

import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.state.ActiveTopicStatistics;

import javax.swing.*;
import java.awt.*;

/**
 * A thin 10px bar rendered above the burn chart that shows where the student's
 * burnStressScore sits within the current zone.
 *
 * Layout:
 *   [prev zone label]     |▓|     [next zone label]
 *
 * The bar spans the score range of the current zone.  The 5px cursor is positioned
 * at the exact fraction of the score within that zone.  Zone data is sourced
 * entirely from ActiveTopicStatistics.ZONE_BOUNDS / ZONE_LABELS / zoneIndexFor()
 * — no duplicate definitions here.
 */
public class BurnHealthZoneBar extends JPanel {

    private static final int    CURSOR_W     =  5 ;
    private static final int    LABEL_MARGIN =  4 ;
    private static final Color  LABEL_COLOR  = Color.GRAY ;

    private final ActiveTopicStatistics ats ;

    public BurnHealthZoneBar( ActiveTopicStatistics ats ) {
        this.ats = ats ;
        setPreferredSize( new Dimension( 0, 14 ) ) ;
        setBackground( UITheme.BG_COLOR ) ;
    }

    @Override
    protected void paintComponent( Graphics g ) {
        super.paintComponent( g ) ;
        int w = getWidth() ; int h = getHeight() ;
        if( w <= 0 || h <= 0 ) return ;

        double score    = ats.getBurnStressScore() ;
        int    zoneIdx  = ActiveTopicStatistics.zoneIndexFor( score ) ;
        double zoneLow  = ActiveTopicStatistics.ZONE_BOUNDS[ zoneIdx ] ;
        double zoneHigh = ActiveTopicStatistics.ZONE_BOUNDS[ zoneIdx + 1 ] ;

        String prevLabel = zoneIdx > 0 ? ActiveTopicStatistics.ZONE_LABELS[ zoneIdx - 1 ] : "" ;
        String nextLabel = zoneIdx < ActiveTopicStatistics.ZONE_LABELS.length-1 ? ActiveTopicStatistics.ZONE_LABELS[ zoneIdx + 1 ] : "" ;

        Graphics2D g2 = (Graphics2D) g.create() ;
        g2.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING,
                             RenderingHints.VALUE_TEXT_ANTIALIAS_ON ) ;

        // Cursor at the score's position within the zone
        double range    = zoneHigh - zoneLow ;
        double fraction = range > 0 ? (score - zoneLow) / range : 0.5 ;
        fraction = Math.max( 0.0, Math.min( 1.0, fraction ) ) ;
        int cx = Math.max( 0, Math.min( w - CURSOR_W, (int) Math.round( fraction * w ) ) ) ;
        g2.setPaint( cursorColor( fraction ) ) ;
        g2.fillRect( cx, 0, CURSOR_W, h ) ;

        // Neighbour zone labels; 8pt keeps ascent < bar height for natural top padding
        g2.setFont( UITheme.BASE_FONT.deriveFont( Font.PLAIN, 10f ) ) ;
        g2.setPaint( LABEL_COLOR ) ;
        FontMetrics fm = g2.getFontMetrics() ;
        int textY = ( h + fm.getAscent() - fm.getDescent() ) / 2 ;

        if( !prevLabel.isEmpty() ) {
            g2.drawString( prevLabel, LABEL_MARGIN, textY ) ;
        }
        if( !nextLabel.isEmpty() ) {
            g2.drawString( nextLabel, w - fm.stringWidth( nextLabel ) - LABEL_MARGIN, textY ) ;
        }

        g2.dispose() ;
    }

    // Green (left) → Gray (center) → Red (right), based on position within the zone bar
    private static Color cursorColor( double fraction ) {
        Color green = Color.getHSBColor( 0.33f, 1.0f, 0.75f ) ;
        Color gray  = Color.GRAY ;
        Color red   = Color.getHSBColor( 0.00f, 1.0f, 0.75f ) ;
        if( fraction <= 0.5 ) {
            return interpolate( green, gray, (float)( fraction * 2.0 ) ) ;
        } else {
            return interpolate( gray, red, (float)( (fraction - 0.5) * 2.0 ) ) ;
        }
    }

    private static Color interpolate( Color a, Color b, float t ) {
        return new Color(
            Math.round( a.getRed()   + t * (b.getRed()   - a.getRed()) ),
            Math.round( a.getGreen() + t * (b.getGreen() - a.getGreen()) ),
            Math.round( a.getBlue()  + t * (b.getBlue()  - a.getBlue()) )
        ) ;
    }
}
