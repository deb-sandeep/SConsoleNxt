package com.sandy.sconsole.core.util;

import com.sandy.sconsole.SConsole;

import java.util.Date;

public class SConsoleUtil {
    
    public static String getElapsedTimeLabel( long seconds, boolean longFormat ) {
        
        int secs    = (int)(seconds) % 60 ;
        int minutes = (int)((seconds / 60) % 60) ;
        int hours   = (int)(seconds / (60*60)) ;
        
        if( longFormat ) {
            return String.format( "%02d:%02d:%02d", hours, minutes, secs ) ;
        }
        return String.format( "%02d:%02d", minutes, secs ) ;
    }

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
