package com.sandy.sconsole.core.ui;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.SConsoleConfig;
import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.endpoints.websockets.controlscreen.AppRemoteWSController;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

import static com.sandy.sconsole.core.ui.screen.Screen.LifecycleMethodType.BEFORE_ACTIVATION;
import static com.sandy.sconsole.core.ui.screen.Screen.LifecycleMethodType.BEFORE_DEACTIVATION;
import static com.sandy.sconsole.core.ui.uiutil.SwingUtils.hideCursor;

@Slf4j
@Component
public class SConsoleFrame extends JFrame {
    
    private final Container contentPane ;
    
    @Autowired private SConsoleConfig config ;
    @Autowired private UITheme        theme ;
    
    @Getter private Screen currentScreen = null ;

    public SConsoleFrame() {
        super() ;
        this.contentPane = super.getContentPane() ;
    }
    
    public void initialize() {
        setUpUI() ;
        if( config.isShowSwingApp() ) {
            makeFrameVisible() ;
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
}
