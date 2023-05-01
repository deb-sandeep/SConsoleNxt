package com.sandy.sconsole.core.ui.uiutil;

import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;

public class UITheme {

    // Screen theme settings
    public Color getBackgroundColor() {
        return Color.BLACK ;
    }
    public String getDefaultScreenIconName() { return "def_screen_icon" ; }

    // Tile theme settings
    public Color getTileBorderColor() { return Color.DARK_GRAY.darker() ; }
    public Border getTileBorder() { return new LineBorder( getTileBorderColor() ) ; }
    public Color getTileForeground() { return Color.GRAY; }

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
