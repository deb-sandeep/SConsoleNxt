package com.sandy.sconsole.initializer;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.behavior.ComponentInitializer;
import com.sandy.sconsole.core.clock.SConsoleClock;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfig;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfigChangeListener;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfigGroup;
import it.sauronsoftware.cron4j.SchedulingPattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@NVPConfigGroup
public class GoodnightScreenStrategy implements ComponentInitializer {

    @NVPConfig private boolean enableStrategy = true ;
    @NVPConfig private String eodCronExpression = "* 23 * * *" ;
    @NVPConfig private String sodCronExpression = "0 9 * * *" ;
    @NVPConfig private String eodScreenName = ScreenManagerInitializer.CLOCK_SCR_NAME ;
    @NVPConfig private String sodScreenName = ScreenManagerInitializer.REFRESHER_SCR_NAME;

    private String eodCallbackTaskId = null ;
    private String sodCallbackTaskId = null ;

    @Override
    public void initialize( SConsole app ) throws Exception {
        deregisterScheduledCallbacks( app.getClock() ) ;
        if( !enableStrategy ) {
            log.info( "->> GoodnightScreenStrategy is disabled." ) ;
        }
        else {
            SchedulingPattern.validate( eodCronExpression ) ;
            SchedulingPattern.validate( sodCronExpression ) ;

            eodCallbackTaskId = registerEODCallback( app ) ;
            sodCallbackTaskId = registerSODCallback( app ) ;
        }
    }

    private String registerEODCallback( SConsole app ) throws Exception {
        return app.getClock().scheduleTask( eodCronExpression, ()->{
            if( app.getFrame() != null ) {
                app.getFrame().changeScreen( eodScreenName ) ;
            }
        } ) ;
    }

    private String registerSODCallback( SConsole app ) throws Exception {
        return app.getClock().scheduleTask( sodCronExpression, ()->{
            if( app.getFrame() != null ) {
                app.getFrame().changeScreen( sodScreenName );
            }
        } ) ;
    }

    @NVPConfigChangeListener
    public void configChanged() {
        try {
            log.debug( "Config change detected. Reinitializing strategy." ) ;
            initialize( SConsole.getApp() ) ;
        }
        catch( Exception e ) {
            log.error( "Could not reinitialize GoodnightScreenStrategy.", e ) ;
        }
    }

    private void deregisterScheduledCallbacks( SConsoleClock clock ) {
        if( eodCallbackTaskId != null ) {
            clock.descheduleTask( eodCallbackTaskId ) ;
            eodCallbackTaskId = null ;
        }

        if( sodCallbackTaskId != null ) {
            clock.descheduleTask( sodCallbackTaskId ) ;
            sodCallbackTaskId = null ;
        }
    }
}
