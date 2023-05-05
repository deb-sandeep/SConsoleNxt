package com.sandy.sconsole;

import com.sandy.sconsole.core.SConsoleConfig;
import com.sandy.sconsole.core.behavior.ComponentFinalizer;
import com.sandy.sconsole.core.behavior.ComponentInitializer;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.clock.SConsoleClock;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfigAnnotationProcessor;
import com.sandy.sconsole.core.ui.SConsoleFrame;
import com.sandy.sconsole.core.ui.screen.ScreenManager;
import com.sandy.sconsole.core.ui.uiutil.DefaultUITheme;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        log.debug( "  Initializing NVPConfig injector." ) ;
        NVPConfigAnnotationProcessor nvpConfigAnnotationProcessor =
                                  new NVPConfigAnnotationProcessor( getCtx() ) ;
        nvpConfigAnnotationProcessor.processNVPConfigAnnotations() ;

        log.debug( "  Calling discovered initializers." ) ;
        discoverAndInvokeInitializers() ;

        log.debug( "  Initializing SConsoleFrame" ) ;
        SwingUtilities.invokeLater( ()->{
            this.frame = new SConsoleFrame( uiTheme, getConfig(),
                    getAppCtx().getBean( ScreenManager.class ) ) ;
        } ) ;

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

    private void discoverAndInvokeInitializers() throws Exception {

        final List<ComponentInitializer> initializers = new ArrayList<>(
                APP_CTX.getBeansOfType( ComponentInitializer.class )
                        .values()
        ) ;

        initializers.sort( (i1, i2) -> i1.getInitializationSequencePreference() -
                                       i2.getInitializationSequencePreference() );

        for( ComponentInitializer si : initializers ) {
            log.debug( "Found system initializer {}. Precedence {}",
                    si.getClass().getName(), si.getInitializationSequencePreference() ) ;

            if( si.isInvocable() ) {
                si.initialize( this );
            }
            else {
                log.debug( "System initializer is not invocable." );
            }
        }
    }

    private void discoverAndInvokeFinalizers() {

        log.debug( "Gracefully shutting down SConsole." ) ;

        final Map<String, ComponentFinalizer> beans =
                APP_CTX.getBeansOfType( ComponentFinalizer.class ) ;

        for( ComponentFinalizer si : beans.values() ) {
            log.debug( "Found system finalzier {}", si.getClass().getName() ) ;
            if( si.isInvocable() ) {
                try {
                    si.destroy( this );
                }
                catch( Exception e ) {
                    log.error( "Error while invoking finalizer.", e );
                }
            }
            else {
                log.debug( "System finalizer is not invocable." );
            }
        }
    }

    // --------------------- Main method ---------------------------------------

    public static void main( String[] args ) {

        log.debug( "Starting Spring Booot..." ) ;

        System.setProperty( "java.awt.headless", "false" ) ;
        SpringApplication.run( SConsole.class, args )
                         .addApplicationListener(
                                 new ApplicationListener<ContextClosedEvent>() {
            public void onApplicationEvent( @NotNull ContextClosedEvent event ) {
                getApp().discoverAndInvokeFinalizers() ;
            }
        } ) ;

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
