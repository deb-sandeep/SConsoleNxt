package com.sandy.sconsole.core.ui;

import com.sandy.sconsole.core.SConsoleConfig;
import com.sandy.sconsole.core.remote.RemoteKeyEvent;
import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.screen.ScreenManager;
import com.sandy.sconsole.core.ui.uiutil.SwingUtils;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.screen.screens.clock.ClockScreen;
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
        setScreen( screenManager.getDefaultScreen() ) ;
        
        if( config.isShowSwingApp() ) {
            setVisible( true ) ;
        }
        else {
            log.info( "[*** IMP ***] Application has been configured not to show the Swing UI" ) ;
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
        
        if( SwingUtils.getScreenWidth() >= 1920 ) {
            int height = Math.min( SwingUtils.getScreenHeight(), 1080 ) ;
            this.setBounds( 0, 0, 1920, height ) ;
        }
        else {
            SwingUtils.setMaximized( this ) ;
        }
    }
    
    void setScreen( Screen screen ) {

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

    public void handleRemoteKeyEvent( RemoteKeyEvent event ) {
        if( this.currentScreen != null ) {
            if( this.currentScreen.getKeySet().isKeyEnabled( event.getKey() ) ) {
                this.currentScreen.processKeyEvent( event ) ;
            }
        }
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
            SwingUtilities.invokeLater( ()->setScreen( nextScreen ) ) ;
        }
    }
}
