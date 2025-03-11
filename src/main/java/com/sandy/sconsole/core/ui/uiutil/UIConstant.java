package com.sandy.sconsole.core.ui.uiutil;

import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.SimpleDateFormat;

public class UIConstant {

    private static final String FONT_NAME = "Courier" ;
    
    public static final Font BASE_FONT = new Font( FONT_NAME, Font.PLAIN, 20 ) ;
    public static final Font SCREENLET_TITLE_FONT = BASE_FONT.deriveFont( Font.BOLD, 60 ) ;

    public static final Font CHART_XAXIS_FONT = new Font( FONT_NAME, Font.PLAIN, 12 ) ;
    public static final Font CHART_YAXIS_FONT = new Font( FONT_NAME, Font.PLAIN, 10 ) ;
    
    public static final Color HISTORIC_BURN_COLOR       = Color.GREEN ;
    public static final Color BASE_BURN_COLOR           = Color.LIGHT_GRAY ;
    public static final Color PROJECTED_VELOCITY_BURN   = Color.RED.brighter() ;
    
    public static final Color TILE_BORDER_COLOR = Color.DARK_GRAY.darker() ;
    public static final Border TILE_BORDER = new LineBorder( TILE_BORDER_COLOR ) ;
    
    public static final SimpleDateFormat DF_TIME_LG = new SimpleDateFormat( "H:mm:ss" ) ;
    public static final SimpleDateFormat DF_TIME_SM = new SimpleDateFormat( "H:mm" ) ;
}
