package com.sandy.sconsole.core.util;

import java.awt.Color;

public class ColorUtil {

    public static String toHtmlColor( Color c ) {
        return String.format( "#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue() ) ;
    }
}
