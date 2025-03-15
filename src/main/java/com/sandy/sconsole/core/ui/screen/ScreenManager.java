package com.sandy.sconsole.core.ui.screen;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.clock.ClockTickListener;
import com.sandy.sconsole.core.clock.SConsoleClock;
import com.sandy.sconsole.core.nvpconfig.NVPManager;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfig;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfigChangeListener;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfigGroup;
import com.sandy.sconsole.core.ui.SConsoleFrame;
import com.sandy.sconsole.endpoints.websockets.controlscreen.AppRemoteWSController;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.sandy.sconsole.core.ui.screen.Screen.LifecycleMethodType.INITIALIZE;
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
    
    // Can't autowire this, else it will cause a cyclic dependency :)
    private AppRemoteWSController wsController ;
    
    @NVPConfig private String endOfDay ;
    @NVPConfig private String startOfDay ;
    
    private final Map<String, Screen> screenMap = new HashMap<>() ;
    
    @Getter private Screen currentScreen ;
    
    private Screen currentRootScreen ;
    private Screen dayRootScreen ;
    private Screen nightRootScreen ;
    
    private long startOfDaySecs ;
    private long endOfDaySecs ;
    
    private final LinkedBlockingQueue<ScreenCmd> cmdQueue = new LinkedBlockingQueue<>() ;
    private final Multimap<String, String> screenTransitions = HashMultimap.create() ;
    
    private int ephemeralLifeSpanLeft = -1 ;
    
    public ScreenManager() {
        super.setDaemon( true ) ;
    }
    
    // ----------------- API methods -------------------------------------------
    // ................. Initialization methods ................................
    public void registerScreen( Screen screen ) {
        
        screen.invokeLifecycleMethod( INITIALIZE ) ;
        
        screenMap.put( screen.getId(), screen ) ;
        
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
    
    public void setDayRootScreen( String screenId ) {
        dayRootScreen = screenMap.get( screenId ) ;
    }
    
    public void setNightRootScreen( String screenId ) {
        nightRootScreen = screenMap.get( screenId ) ;
    }
    
    public void addScreenTransitions( Screen sourceScreen,
                                      String targetScreenId, String ...targetScreenIds ) {
        
        screenTransitions.put( sourceScreen.getId(), targetScreenId ) ;
        Arrays.stream( targetScreenIds )
              .iterator()
              .forEachRemaining( tgt -> screenTransitions.put( sourceScreen.getId(), tgt ) ) ;
    }
    
    // Call this method after all the screens have been registered and
    // day/night root screens have been set.
    public void init() {
        wsController = SConsole.getBean( AppRemoteWSController.class ) ;
        clock.addTickListener( this, TimeUnit.SECONDS ) ;
        loadConfig() ;
        super.start() ;
    }
    
    public void execute( ScreenCmd cmd ) {
        this.cmdQueue.add( cmd ) ;
    }
    
    // -------------------- Public external methods ----------------------------
    public Collection<String> getScreenTransitions( String screenId ) {
        return screenTransitions.get( screenId ) ;
    }
    
    public void scheduleScreenChange( String screenId ) {
        execute( CHANGE_SCREEN_CMD( screenId ) ) ;
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
            scheduleScreenChange( currentRootScreen.getId() ) ;
        }
    }
    
    @Override
    public void secondTicked( Calendar calendar ) { // Ticks at second interval
        refreshRootScreenIfApplicable() ;
        updateScreenLongevity() ;
    }
    
    private void updateScreenLongevity() {
        if( currentScreen.isEphemeral() && ephemeralLifeSpanLeft > 0 ) {
            ephemeralLifeSpanLeft-- ;
            wsController.sendScreenTimeLeft( ephemeralLifeSpanLeft ) ;
            if( ephemeralLifeSpanLeft == 0 ) {
                scheduleScreenChange( currentRootScreen.getId() ) ;
            }
        }
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
            setCurrentScreen( screen ) ;
        }
        else {
            if( currentScreen != screen &&
                ( screen.getPriority() >= currentScreen.getPriority() ) ) {
                setCurrentScreen( screen ) ;
            }
        }
    }
    
    private void setCurrentScreen( Screen screen ) {
        currentScreen = screen ;
        mainFrame.setScreen( screen ) ;
        
        if( currentScreen != currentRootScreen && currentScreen.isEphemeral() ) {
            ephemeralLifeSpanLeft = currentScreen.getEphemeralLifeSpan() ;
        }
        else {
            ephemeralLifeSpanLeft = 0 ;
        }
        wsController.sendScreenTimeLeft( ephemeralLifeSpanLeft ) ;
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
