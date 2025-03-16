package com.sandy.sconsole.core.ui.uiutil;

import org.springframework.stereotype.Component;

import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;

@Component
public class UITheme {
    
    public static final String FONT_NAME = "Courier" ;
    public static final Font   BASE_FONT = new Font( FONT_NAME, Font.PLAIN, 20 ) ;
    public static final Color  TILE_BORDER_COLOR = SwingUtils.darkerColor( Color.DARK_GRAY, 0.5F ) ;
    public static final Border TILE_BORDER = new LineBorder( TILE_BORDER_COLOR ) ;
    public static final Color  BG_COLOR = Color.BLACK ;
    public static final Color  TILE_FG_COLOR = Color.GRAY.darker() ;

    public Font getLabelFont( int style, int size ) {
        return BASE_FONT.deriveFont( style, size ) ;
    }

    public Font getLabelFont( int size ) {
        return BASE_FONT.deriveFont( Font.PLAIN, size ) ;
    }
}
