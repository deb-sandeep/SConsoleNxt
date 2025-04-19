package com.sandy.sconsole.core.ui;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.SConsoleConfig;
import com.sandy.sconsole.core.clock.ClockTickListener;
import com.sandy.sconsole.core.clock.SConsoleClock;
import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.endpoints.websockets.controlscreen.AppRemoteWSController;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.sandy.sconsole.core.ui.screen.Screen.LifecycleMethodType.BEFORE_ACTIVATION;
import static com.sandy.sconsole.core.ui.screen.Screen.LifecycleMethodType.BEFORE_DEACTIVATION;
import static com.sandy.sconsole.core.ui.uiutil.SwingUtils.hideCursor;

@Slf4j
@Component
public class SConsoleFrame extends JFrame
    implements ClockTickListener {
    
    private static final SimpleDateFormat DF = new SimpleDateFormat( "yyyy-MM-dd HHmmss" ) ;
    
    private final Container contentPane ;
    
    @Autowired private SConsoleConfig config ;
    @Autowired private UITheme theme ;
    @Autowired private SConsoleClock clock ;
    
    @Getter private Screen currentScreen = null ;

    public SConsoleFrame() {
        super() ;
        this.contentPane = super.getContentPane() ;
    }
    
    public void initialize() {
        setUpUI() ;
        if( config.isShowSwingApp() ) {
            makeFrameVisible() ;
            clock.addTickListenerForSingleTimeUnit( this, TimeUnit.MINUTES ) ;
        }
        else {
            log.info( "[*** IMP ***] Application has been configured not to show the Swing UI" ) ;
        }
    }
    
    public void setScreen( @NonNull Screen screen ) {
        
        SwingUtilities.invokeLater( () -> {
            log.debug( "Setting screen to {}", screen.getScreenName() );
            if( currentScreen != null ) {
                log.debug( "  Deactivating current screen. {}", currentScreen.getScreenName() ) ;
                currentScreen.invokeLifecycleMethod( BEFORE_DEACTIVATION ) ;
                contentPane.remove( currentScreen );
            }
            
            currentScreen = screen ;
            
            log.debug( "  Activating new screen. {}", currentScreen.getScreenName() ) ;
            currentScreen.invokeLifecycleMethod( BEFORE_ACTIVATION ) ;
            
            contentPane.add( currentScreen, BorderLayout.CENTER ) ;
            contentPane.revalidate() ;
            contentPane.repaint() ;
            
            log.debug( "Sending screen change message to any connected clients" ) ;
            // Send a notification to any browsers connected to the server
            // to change their remote control screen
            AppRemoteWSController webSocket = SConsole.getBean( AppRemoteWSController.class ) ;
            webSocket.sendPeerScreenDisplayMsg() ;
            log.debug( "Setting screen completed" );
        } ) ;
    }

    private void makeFrameVisible() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment() ;
        GraphicsDevice[] devices = ge.getScreenDevices() ;
        if( devices.length > 1 ) {
            for( GraphicsDevice d : devices ) {
                if( d.getDefaultConfiguration().getBounds().height == 1080 ) {
                    d.setFullScreenWindow( this ) ;
                    super.setVisible( true ) ;
                    break ;
                }
            }
        }
        else {
            setVisible( true ) ;
        }
    }
    
    private void setUpUI() {
        
        setUndecorated( true ) ;
        setResizable( false ) ;
        hideCursor( contentPane ) ;
        
        contentPane.setBackground( UITheme.BG_COLOR ) ;
        contentPane.setLayout( new BorderLayout() ) ;

        this.setBounds( 0,0, 1920, 1080 ) ;
    }
    
    @Override
    public void minuteTicked( Calendar calendar ) {
        int minute = calendar.get( Calendar.MINUTE ) ;
        int hour = calendar.get( Calendar.HOUR_OF_DAY ) ;
        if( hour > 8 || hour < 1 ) {
            if( minute % 5 == 0 ) {
                try {
                    captureScreenshot() ;
                }
                catch( Exception e ) {
                    log.error( "Unable to capture screenshot", e ) ;
                }
            }
        }
    }
    
    public void captureScreenshot()
            throws InterruptedException, InvocationTargetException {
        
        File dir = new File( config.getWorkspacePath(), "screenshots" ) ;
        if( !dir.exists() ) {
            dir.mkdirs() ;
        }
        
        File file = new File( dir, DF.format( new Date() ) + ".png" ) ;
        
        SwingUtilities.invokeAndWait( () -> {
            BufferedImage img = new BufferedImage( getWidth(),
                                                   getHeight(),
                                                   BufferedImage.TYPE_INT_RGB ) ;
            paint( img.getGraphics() ) ;
            
            try {
                ImageIO.write( img, "png", file ) ;
            }
            catch( IOException e ) {
                log.error( "Unable to save image - {}", file.getAbsolutePath(), e );
            }
        } ) ;
    }
}
