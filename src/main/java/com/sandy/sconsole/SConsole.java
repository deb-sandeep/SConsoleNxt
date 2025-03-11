package com.sandy.sconsole;

import com.sandy.sconsole.core.SConsoleConfig;
import com.sandy.sconsole.core.clock.SConsoleClock;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfigAnnotationProcessor;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.ui.SConsoleFrame;
import com.sandy.sconsole.ui.screen.ScreenManager;
import com.sandy.sconsole.dao.quote.QuoteManager;
import com.sandy.sconsole.ui.screen.clock.ClockScreen;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.swing.*;

@Slf4j
@SpringBootApplication
public class SConsole
        implements ApplicationContextAware, WebMvcConfigurer {

    private static ConfigurableApplicationContext APP_CTX = null ;
    private static SConsole APP = null;

    public static SConsole getApp() {
        return APP;
    }

    public static ApplicationContext getAppCtx() {
        return APP_CTX ;
    }

    // ---------------- Instance methods start ---------------------------------
    
    @Autowired private SConsoleClock  clock ;
    @Autowired private SConsoleFrame  frame ;
    @Autowired private QuoteManager   quoteManager ;
    @Autowired private NVPConfigAnnotationProcessor nvpAnnotationProcessor ;
    @Autowired private ScreenManager screenManager ;
    
    @Autowired @Getter private SConsoleConfig config ;
    @Autowired @Getter private UITheme uiTheme ;
    
    @Autowired private ClockScreen clockScreen ;
    
    public SConsole() {
        APP = this;
    }

    @Override
    public void setApplicationContext( ApplicationContext applicationContext )
            throws BeansException {
        APP_CTX = ( ConfigurableApplicationContext )applicationContext;
    }

    public void initialize() {

        log.debug( "## Initializing SConsole app. >" ) ;

        log.debug( "- Initializing Clock" ) ;
        this.clock.initialize() ;

        log.debug( "- Initializing NVPConfig injector." ) ;
        nvpAnnotationProcessor.processNVPConfigAnnotations() ;

        log.debug( "- Initializing QuoteManager." ) ;
        quoteManager.initialize( this ) ;
        
        log.debug( "- Initializing ScreenManager." ) ;
        initializeScreenManager() ;

        log.debug( "- Initializing SConsoleFrame" ) ;
        SwingUtilities.invokeLater( () -> this.frame.initialize() ) ;

        log.debug( "<< ## SConsole initialization complete" ) ;
    }
    
    private void initializeScreenManager() {
        screenManager.registerScreen( clockScreen ) ;
    }
    
    private void invokeFinalizers() {
    }

    // --------------------- Main method ---------------------------------------

    public static void main( String[] args ) {

        log.debug( "Starting Spring Boot..." ) ;

        System.setProperty( "java.awt.headless", "false" ) ;
        SpringApplication.run( SConsole.class, args )
                         .addApplicationListener(
                                 ( ApplicationListener<ContextClosedEvent> )event ->
                                         getApp().invokeFinalizers()
                         ) ;

        log.debug( "Starting SConsole.." ) ;
        SConsole app = SConsole.getAppCtx().getBean( SConsole.class ) ;
        try {
            app.initialize() ;
        }
        catch( Exception e ) {
            log.error( "Exception while initializing SConsole.", e ) ;
            System.exit( -1 ) ;
        }
    }
}
