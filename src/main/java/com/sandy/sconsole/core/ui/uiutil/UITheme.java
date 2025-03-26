package com.sandy.sconsole.core.ui.uiutil;

import org.springframework.stereotype.Component;

import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;

@Component
public class UITheme {
    
    public static final int GRID_WIDTH = 1920/16 ;
    public static final int GRID_HEIGHT = 1080/32 ;
    
    public static final String FONT_NAME = "Courier" ;
    public static final Font   BASE_FONT = new Font( FONT_NAME, Font.PLAIN, 20 ) ;
    public static final Color  TILE_BORDER_COLOR = SwingUtils.darkerColor( Color.DARK_GRAY, 0.5F ) ;
    public static final Border TILE_BORDER = new LineBorder( TILE_BORDER_COLOR ) ;
    public static final Color  BG_COLOR = Color.BLACK ;
    public static final Color  TILE_FG_COLOR = Color.GRAY.darker() ;
    
    public static final Font CHART_XAXIS_FONT = new Font( FONT_NAME, Font.PLAIN, 12 ) ;
    public static final Font CHART_YAXIS_FONT = new Font( FONT_NAME, Font.PLAIN, 10 ) ;
    
    public static final Color HISTORIC_BURN_COLOR = Color.GREEN ;
    public static final Color BASE_BURN_COLOR = Color.GRAY ;
    public static final Color HISTORIC_BURN_REGRESSION_COLOR = Color.RED.brighter() ;
    
    public Font getLabelFont( int style, int size ) {
        return BASE_FONT.deriveFont( style, size ) ;
    }

    public Font getLabelFont( int size ) {
        return BASE_FONT.deriveFont( Font.PLAIN, size ) ;
    }
}
