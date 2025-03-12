package com.sandy.sconsole.core.ui.screen;

import com.sandy.sconsole.core.clock.ClockTickListener;
import com.sandy.sconsole.core.clock.SConsoleClock;
import com.sandy.sconsole.core.nvpconfig.NVPManager;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfig;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfigChangeListener;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfigGroup;
import com.sandy.sconsole.core.ui.SConsoleFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.sandy.sconsole.core.ui.screen.ScreenCmd.CHANGE_SCREEN_CMD;

@Slf4j
@Component
@NVPConfigGroup( groupName = "ScreenManager" )
public class ScreenManager extends Thread implements ClockTickListener {
    
    private static final SimpleDateFormat CONFIG_DF   = new SimpleDateFormat( "HH:mm:ss" ) ;
    private static final String DEFAULT_EOD = "23:30:00" ; // End of day
    private static final String DEFAULT_SOD = "06:00:00" ; // Start of day
    
    @Autowired private SConsoleClock clock ;
    @Autowired private NVPManager nvpManager ;
    @Autowired private SConsoleFrame mainFrame ;
    
    @NVPConfig private String endOfDay ;
    @NVPConfig private String startOfDay ;
    
    private final Map<String, Screen> screenMap = new HashMap<>() ;
    private final Map<String, Integer> screenPriority = new HashMap<>() ;
    
    private Screen currentScreen ;
    private Screen currentRootScreen ;
    
    private Screen dayRootScreen ;
    private Screen nightRootScreen ;
    
    private long startOfDaySecs ;
    private long endOfDaySecs ;
    
    private final LinkedBlockingQueue<ScreenCmd> cmdQueue = new LinkedBlockingQueue<>() ;
    
    public ScreenManager() {
        super.setDaemon( true ) ;
    }
    
    // ----------------- API methods -------------------------------------------
    // ................. Initialization methods ................................
    public void registerScreen( Screen screen, int priority ) {
        
        screen.initialize() ;
        screenMap.put( screen.getName(), screen ) ;
        screenPriority.put( screen.getName(), priority ) ;
        
        // Convenience logic to treat the first and the second registrations
        // as the root screens for night and day respectively.
        if( nightRootScreen == null ) {
            nightRootScreen = screen ;
            dayRootScreen = screen ;
        }
        else if( dayRootScreen == null ) {
            dayRootScreen = screen ;
        }
    }
    
    public void setDayRootScreen( String dayRootScreenName ) {
        dayRootScreen = screenMap.get( dayRootScreenName ) ;
    }
    
    public void setNightRootScreen( String nightRootScreenName ) {
        nightRootScreen = screenMap.get( nightRootScreenName ) ;
    }
    
    // Call this method after all the screens have been registered and
    // day/night root screens have been set.
    public void init() {
        clock.addTickListener( this, TimeUnit.SECONDS ) ;
        loadConfig() ;
        super.start() ;
    }
    
    public void execute( ScreenCmd cmd ) {
        this.cmdQueue.add( cmd ) ;
    }
    
    // -------------------- Internal methods -----------------------------------
    private void loadConfig() {
        try {
            nvpManager.loadNVPConfigState( this ) ;
        }
        catch( Exception e ) {
            log.error( "Failed to load NVP config for ScreenManager. Resorting to default values", e ) ;
            startOfDay = "06:00:00" ;
            endOfDay = "23:30:00" ;
        }
        parseConfigDates() ;
    }
    
    @NVPConfigChangeListener
    public void refreshConfig() {
        log.debug( "Screen Manager config change detected" ) ;
        parseConfigDates() ;
    }
    
    private void parseConfigDates() {
        try {
            startOfDaySecs = secondsSinceStartOfDay( startOfDay == null ? DEFAULT_SOD : startOfDay ) ;
            endOfDaySecs = secondsSinceStartOfDay( endOfDay == null ? DEFAULT_EOD : endOfDay ) ;
        }
        catch( ParseException e ) {
            log.error( "Failed to parse config dates for ScreenManager. Resorting to default values", e ) ;
            try {
                startOfDaySecs = secondsSinceStartOfDay( DEFAULT_SOD ) ;
                endOfDaySecs = secondsSinceStartOfDay( DEFAULT_EOD ) ;
            }
            catch( ParseException ignore ) {
                // Ignore the exception. This will never happen
            }
        }
    }
    
    private synchronized void refreshRootScreenIfApplicable() {
        
        long secsTillNow = secondsSinceStartOfDay( Calendar.getInstance() ) ;
        
        Screen newRootScreen = dayRootScreen ;
        if( secsTillNow >= endOfDaySecs || secsTillNow < startOfDaySecs ) {
            newRootScreen = nightRootScreen ;
        }
        
        if( currentRootScreen == null || newRootScreen != currentRootScreen ) {
            currentRootScreen = newRootScreen ;
            execute( CHANGE_SCREEN_CMD( currentRootScreen.getName() ) );
        }
    }
    
    @Override
    public void clockTick( Calendar calendar ) { // Ticks at second interval
        refreshRootScreenIfApplicable() ;
    }
    
    public void run() {
        refreshRootScreenIfApplicable() ;
        while( true ) {
            try {
                ScreenCmd cmd = cmdQueue.take() ;
                switch( cmd.getType() ) {
                    case CHANGE_SCREEN -> _handleChangeScreenCmd( cmd ) ;
                }
            }
            catch( Exception e ) {
                log.error( "Failed to execute screen command", e ) ;
                // Don't throw, there is no one up there to catch.
            }
        }
    }
    
    private void _handleChangeScreenCmd( ScreenCmd cmd ) {
        
        Screen screen = screenMap.get( cmd.getNewScreenName() ) ;
        if( currentScreen == null ) {
            currentScreen = screen ;
            mainFrame.setScreen( screen ) ;
        }
        else {
            if( currentScreen != screen &&
                ( getScreenPriority( currentScreen ) <= getScreenPriority( screen ) ) ) {
                currentScreen = screen ;
                mainFrame.setScreen( screen ) ;
            }
        }
    }
    
    private int getScreenPriority( Screen screen ) {
        return screenPriority.get( screen.getName() ) ;
    }
    
    private long secondsSinceStartOfDay( String dateStr ) throws ParseException {
        Calendar cfgDate = Calendar.getInstance() ;
        cfgDate.setTime( CONFIG_DF.parse( dateStr ) ) ;
        return secondsSinceStartOfDay( cfgDate ) ;
    }
    
    private long secondsSinceStartOfDay( Calendar calendar ) {
        int hour = calendar.get( Calendar.HOUR_OF_DAY ) ;
        int minute = calendar.get( Calendar.MINUTE ) ;
        int second = calendar.get( Calendar.SECOND ) ;
        return hour * 3600 + minute * 60 + second ;
    }
}
