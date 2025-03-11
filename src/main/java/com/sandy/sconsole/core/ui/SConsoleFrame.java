package com.sandy.sconsole.core.ui;

import com.sandy.sconsole.core.SConsoleConfig;
import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.screen.ScreenManager;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.screen.clock.ClockScreen;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

import static com.sandy.sconsole.core.ui.uiutil.SwingUtils.hideCursor;

@Slf4j
public class SConsoleFrame extends JFrame {
    
    private final Container contentPane ;
    private final ScreenManager screenManager ;

    @Getter private Screen currentScreen = null ;

    public SConsoleFrame( UITheme theme, SConsoleConfig config, ScreenManager screenManager ) {
        super() ;

        this.contentPane = super.getContentPane() ;
        this.screenManager = screenManager ;

        setUpUI( theme ) ;
        
        if( config.isShowSwingApp() ) {
            makeFrameVisible() ;
        }
        else {
            log.info( "[*** IMP ***] Application has been configured not to show the Swing UI" ) ;
        }
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
    
    private void setUpUI( UITheme theme ) {
        
        setUndecorated( true ) ;
        setResizable( false ) ;
        hideCursor( contentPane ) ;
        
        contentPane.setBackground( theme.getBackgroundColor() ) ;
        contentPane.setLayout( new BorderLayout() ) ;

        JPanel panel = new ClockScreen() ;

        contentPane.add( panel ) ;

        this.setBounds( 0,0, 1920, 1080 ) ;
    }
    
    void setActiveScreen( Screen screen ) {

        if( screen == null ) return ;

        if( currentScreen != null ) {
            currentScreen.beforeDeactivation() ;
            contentPane.remove( currentScreen ) ;
        }

        currentScreen = screen ;
        currentScreen.beforeActivation() ;

        contentPane.add( currentScreen, BorderLayout.CENTER ) ;
        contentPane.revalidate() ;
        contentPane.repaint() ;
    }

    public void changeScreen( String nextScreenName ) {

        log.debug( "Changing screen to '{}'", nextScreenName ) ;
        if( this.currentScreen != null &&
                this.currentScreen.getName().equals( nextScreenName ) ) {
            log.debug( "  Requested screen {} is already active.", nextScreenName ) ;
            return ;
        }

        Screen nextScreen = this.screenManager.getScreen( nextScreenName ) ;
        if( nextScreen == null ) {
            log.info( "  Screen '{}' not registered.", nextScreenName ) ;
        }
        else {
            SwingUtilities.invokeLater( ()-> setActiveScreen( nextScreen ) ) ;
        }
    }
}
