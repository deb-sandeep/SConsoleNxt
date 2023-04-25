package com.sandy.sconsole.core.ui;

import com.sandy.sconsole.core.SConsoleConfig;
import com.sandy.sconsole.core.ui.uiutil.SwingUtils;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.screen.clock.ClockScreen;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

import static com.sandy.sconsole.core.ui.uiutil.SwingUtils.hideCursor;

@Slf4j
public class SConsoleFrame extends JFrame {
    
    private final Container contentPane ;

    private Screen currentScreen = null ;

    public SConsoleFrame( UITheme theme, SConsoleConfig config, ScreenManager screenManager ) {
        super() ;

        this.contentPane = super.getContentPane() ;

        setUpUI( theme ) ;
        setCenterPanel( screenManager.getDefaultScreen() ) ;
        
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
    
    void setCenterPanel( Screen screen ) {

        log.debug( "Setting screen {}.", screen  ) ;
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
}
