package com.sandy.sconsole.core.ui.uiutil;

import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;

public class UIConstant {

    private static final String FONT_NAME = "Courier" ;
    
    public static final Font BASE_FONT = new Font( FONT_NAME, Font.PLAIN, 20 ) ;

    public static final Font CHART_XAXIS_FONT = new Font( FONT_NAME, Font.PLAIN, 12 ) ;
    public static final Font CHART_YAXIS_FONT = new Font( FONT_NAME, Font.PLAIN, 10 ) ;
    
    public static final Color TILE_BORDER_COLOR = SwingUtils.darkerColor( Color.DARK_GRAY, 0.5F ) ;
    public static final Border TILE_BORDER = new LineBorder( TILE_BORDER_COLOR ) ;
}
