package com.sandy.sconsole.ui.screen;

import com.sandy.sconsole.core.clock.ClockTickListener;
import com.sandy.sconsole.core.clock.SConsoleClock;
import com.sandy.sconsole.core.nvpconfig.NVPManager;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfig;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfigChangeListener;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfigGroup;
import com.sandy.sconsole.core.ui.screen.Screen;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@NVPConfigGroup( groupName = "ScreenManager" )
public class ScreenManager implements ClockTickListener {
    
    private static final SimpleDateFormat CFG_DF = new SimpleDateFormat( "HH:mm:ss" ) ;
    private static final String DEF_EOD = "23:30:00" ;
    private static final String DEF_SOD = "06:00:00" ;
    
    @Autowired private SConsoleClock clock ;
    @Autowired private NVPManager nvpManager ;
    
    private final Map<String, Screen> screenMap = new HashMap<>() ;
    
    private Screen currentScreen ;
    private Screen rootScreen ;
    private Screen dayRootScreen ;
    private Screen nightRootScreen ;
    
    @NVPConfig private String endOfDay ;
    @NVPConfig private String startOfDay ;
    
    private long startOfDaySecs ;
    private long endOfDaySecs ;
    
    public ScreenManager() {}
    
    @PostConstruct
    public void init() {
        clock.addTickListener( this, TimeUnit.MINUTES ) ;
        refreshConfig() ;
    }
    
    private void refreshConfig() {
        try {
            log.debug( "Refreshing configuration for screen manager" ) ;
            nvpManager.loadNVPConfigState( this ) ;
        }
        catch( Exception e ) {
            log.error( "Failed to load NVP config for ScreenManager. Resorting to default values", e ) ;
            startOfDay = "06:00:00" ;
            endOfDay = "23:30:00" ;
        }
    }
    
    @NVPConfigChangeListener
    public void configChanged( com.sandy.sconsole.core.nvpconfig.NVPConfig nvpConfig ) {
        parseConfigDates() ;
    }
    
    private void parseConfigDates() {
        try {
            startOfDaySecs = secondsSinceStartOfDay( startOfDay == null ? DEF_SOD : startOfDay) ;
            endOfDaySecs = secondsSinceStartOfDay( endOfDay == null ? DEF_EOD : endOfDay ) ;
        }
        catch( ParseException e ) {
            log.error( "Failed to parse config dates for ScreenManager. Resorting to default values", e ) ;
            try {
                startOfDaySecs = secondsSinceStartOfDay( DEF_SOD ) ;
                endOfDaySecs = secondsSinceStartOfDay( DEF_EOD ) ;
            }
            catch( ParseException ignore ) {
                // Ignore the exception. This will never happen
            }
        }
    }
    
    public void registerScreen( Screen screen ) {
        
        screen.initialize() ;
        screenMap.put( screen.getName(), screen ) ;
        
        // Convenience logic to treat the first and the second registrations
        // as the root screens for night and day respectively.
        if( nightRootScreen == null ) {
            nightRootScreen = screen ;
        }
        else if( dayRootScreen == null ) {
            dayRootScreen = screen ;
        }
        
        computeRootScreen() ;
        this.currentScreen = rootScreen ;
    }
    
    public Screen getActiveScreen() {
        return this.currentScreen ;
    }
    
    @Override
    public void clockTick( Calendar calendar ) { // Ticks at minute intervals
    }
    
    private void computeRootScreen() {
        Calendar now = Calendar.getInstance() ;
        long secsTillNow = secondsSinceStartOfDay( now ) ;

        rootScreen = ( secsTillNow >= endOfDaySecs || secsTillNow < startOfDaySecs ) ?
                                                nightRootScreen : dayRootScreen ;
    }
    
    private long secondsSinceStartOfDay( String dateStr ) throws ParseException {
        Calendar cfgDate = Calendar.getInstance() ;
        cfgDate.setTime( CFG_DF.parse( dateStr ) ) ;
        return secondsSinceStartOfDay( cfgDate ) ;
    }
    
    private long secondsSinceStartOfDay( Calendar calendar ) {
        int hour = calendar.get( Calendar.HOUR_OF_DAY ) ;
        int minute = calendar.get( Calendar.MINUTE ) ;
        int second = calendar.get( Calendar.SECOND ) ;
        return hour * 3600 + minute * 60 + second ;
    }
}
