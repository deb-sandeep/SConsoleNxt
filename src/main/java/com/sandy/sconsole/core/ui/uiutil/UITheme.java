package com.sandy.sconsole.core.ui.uiutil;

import org.springframework.stereotype.Component;

import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;

@Component
public class UITheme {

    // Screen theme settings
    public Color getBackgroundColor() {
        return Color.BLACK ;
    }
    public String getDefaultScreenIconName() { return "def_screen_icon" ; }

    // Tile theme settings
    public Color getTileBorderColor() { return SwingUtils.darkerColor( Color.DARK_GRAY, 0.5F ) ; }
    public Border getTileBorder() { return new LineBorder( getTileBorderColor() ) ; }
    public Color getTileForeground() { return Color.GRAY.darker(); }

    // Label theme settings
    public String getLabelFontName() { return "Courier" ; }
    public Font getLabelBaseFont() { return new Font( getLabelFontName(), Font.PLAIN, 20 ) ; }

    public Font getLabelFont( int style, int size ) {
        return getLabelBaseFont().deriveFont( style, size ) ;
    }

    public Font getLabelFont( int size ) {
        return getLabelBaseFont().deriveFont( Font.PLAIN, size ) ;
    }
}
