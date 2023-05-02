package com.sandy.sconsole;

import com.sandy.sconsole.core.SConsoleConfig;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.clock.SConsoleClock;
import com.sandy.sconsole.core.ui.SConsoleFrame;
import com.sandy.sconsole.core.ui.screen.ScreenManager;
import com.sandy.sconsole.core.ui.uiutil.DefaultUITheme;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.initializer.ScreenManagerInitializer;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@SpringBootApplication
public class SConsole
        implements ApplicationContextAware, WebMvcConfigurer {

    private static final EventBus GLOBAL_EVENT_BUS = new EventBus() ;

    private static ApplicationContext APP_CTX = null ;
    private static SConsole APP = null;

    public static SConsole getApp() {
        return APP;
    }

    public static ApplicationContext getAppCtx() {
        return APP_CTX ;
    }

    public static EventBus getBus() {
        return GLOBAL_EVENT_BUS ;
    }

    // ---------------- Instance methods start ---------------------------------

    private final SConsoleClock clock = new SConsoleClock() ;

    private UITheme uiTheme = null ;
    private SConsoleFrame frame = null ;
    private SConsoleConfig cfg = null ;

    public SConsole() {
        APP = this;
    }

    @Override
    public void setApplicationContext( @NotNull ApplicationContext applicationContext )
            throws BeansException {
        APP_CTX = applicationContext;
    }

    public void initialize() throws Exception {

        log.debug( "Initializing SConsole app." ) ;

        log.debug( "  Initializing Clock" ) ;
        this.clock.initialize() ;

        log.debug( "  Initializing Theme" ) ;
        this.uiTheme = new DefaultUITheme() ;

        log.debug( "  Initializing ScreenManager" ) ;
        ScreenManager screenManager = APP_CTX.getBean( ScreenManager.class ) ;
        new ScreenManagerInitializer().initialize( this.uiTheme, screenManager ) ;

        log.debug( "  Initializing SConsoleFrame" ) ;
        this.frame = new SConsoleFrame( uiTheme, getConfig(), screenManager ) ;

        log.debug( "SConsole initialization complete" ) ;
    }

    public UITheme getTheme() { return this.uiTheme ; }

    public SConsoleFrame getFrame() { return this.frame; }

    public SConsoleClock getClock() { return this.clock; }

    public ApplicationContext getCtx() { return SConsole.APP_CTX ; } ;

    public SConsoleConfig getConfig() {
        if( cfg == null ) {
            if( APP_CTX != null ) {
                cfg = ( SConsoleConfig )APP_CTX.getBean("config");
            }
            else {
                cfg = new SConsoleConfig() ;
            }
        }
        return cfg ;
    }

    // --------------------- Main method ---------------------------------------

    public static void main( String[] args ) {

        log.debug( "Starting Spring Booot..." ) ;

        System.setProperty( "java.awt.headless", "false" ) ;
        SpringApplication.run( SConsole.class, args ) ;

        log.debug( "Starting SConsole.." ) ;
        SConsole app = SConsole.getAppCtx().getBean( SConsole.class ) ;
        try {
            app.initialize() ;
        }
        catch( Exception e ) {
            log.error( "Exception while initializing SConsole.", e ) ;
        }
    }
}
