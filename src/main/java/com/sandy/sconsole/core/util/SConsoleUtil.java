package com.sandy.sconsole.core.util;

import com.sandy.sconsole.SConsole;

import java.time.Duration;
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
    
    public static int duration( Date start, Date end ) {
        return (int)Duration.between( start.toInstant(), end.toInstant() ).toDays() ;
    }
}
