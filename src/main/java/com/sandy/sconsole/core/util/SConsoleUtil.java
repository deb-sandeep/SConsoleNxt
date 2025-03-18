package com.sandy.sconsole.core.util;

import com.sandy.sconsole.SConsole;

import java.util.Date;

public class SConsoleUtil {
    
    public static boolean isOperatingOnPiMon() {
        return SConsole.getApp()
                       .getConfig()
                       .getEnvType()
                       .equalsIgnoreCase( "PROD" ) ;
    }
    
    public static boolean isBetween( Date start, Date end, Date date ) {
        return date.after( start ) && date.before( end ) ;
    }
}
