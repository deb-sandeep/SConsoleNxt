package com.sandy.sconsole.ui.screen.session.tile.center.face;

import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.core.ui.uiutil.SwingUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

@Component
@Scope( "prototype" )
public class TheoryTileFace extends Tile {

    private static final int SLIDE_INTERVAL_MS = 10_000 ;

    private static final String[] SLIDE_RESOURCE_PATHS = {
            "/img/iit-bombay.png",
            "/img/iit-chennai.png",
            "/img/iit-delhi.png",
            "/img/iit-kanpur.png",
            "/img/iit-kgp.png"
    } ;

    private final CardLayout cardLayout = new CardLayout() ;
    private final Object activationLock = new Object() ;
    private final Object slideIndexLock = new Object() ;

    private volatile boolean slideshowActive = false ;
    private int currentSlideIndex = 0 ;

    public TheoryTileFace() {
        setUpUI() ;
        createSlideShowThread().start() ;
    }

    @Override
    public void beforeActivation() {
        synchronized( activationLock ) {
            slideshowActive = true ;
            activationLock.notifyAll() ;
        }
        showSlide( getCurrentSlideIndex() ) ;
    }

    @Override
    public void beforeDeactivation() {
        slideshowActive = false ;
    }

    private void setUpUI() {

        setBackground( Color.BLACK ) ;
        setLayout( cardLayout ) ;

        for( int i = 0; i < SLIDE_RESOURCE_PATHS.length; i++ ) {
            JLabel slideLabel = SwingUtils.createEmptyLabel( theme ) ;
            slideLabel.setIcon( new ImageIcon( loadSlideImage( SLIDE_RESOURCE_PATHS[i] ) ) ) ;
            add( slideLabel, getCardName( i ) ) ;
        }
        showSlide( getCurrentSlideIndex() ) ;
    }

    private Thread createSlideShowThread() {
        Thread thread = new Thread( this::runSlideShowLoop, "TheoryTileFaceSlideShow" ) ;
        thread.setDaemon( true ) ;
        return thread ;
    }

    private void runSlideShowLoop() {

        while( !Thread.currentThread().isInterrupted() ) {
            if( !waitForActivation() ) {
                return ;
            }
            
            try {
                Thread.sleep( SLIDE_INTERVAL_MS ) ;
            }
            catch( InterruptedException e ) {
                Thread.currentThread().interrupt() ;
                return ;
            }

            if( slideshowActive ) {
                advanceSlide() ;
            }
        }
    }

    private boolean waitForActivation() {

        synchronized( activationLock ) {
            while( !slideshowActive ) {
                try {
                    activationLock.wait() ;
                }
                catch( InterruptedException e ) {
                    Thread.currentThread().interrupt() ;
                    return false ;
                }
            }
        }
        return true ;
    }

    private void advanceSlide() {
        showSlide( incrementAndGetCurrentSlideIndex() ) ;
    }

    private int getCurrentSlideIndex() {
        synchronized( slideIndexLock ) {
            return currentSlideIndex ;
        }
    }

    private int incrementAndGetCurrentSlideIndex() {
        synchronized( slideIndexLock ) {
            currentSlideIndex = ( currentSlideIndex + 1 ) % SLIDE_RESOURCE_PATHS.length ;
            return currentSlideIndex ;
        }
    }

    private void showSlide( int slideIndex ) {
        SwingUtilities.invokeLater( () -> cardLayout.show( this, getCardName( slideIndex ) ) ) ;
    }

    private String getCardName( int slideIndex ) {
        return "SLIDE_" + slideIndex ;
    }

    private BufferedImage loadSlideImage( String resourcePath ) {

        URL resource = TheoryTileFace.class.getResource( resourcePath ) ;
        if( resource == null ) {
            throw new IllegalStateException( "Slide resource not found: " + resourcePath ) ;
        }

        try {
            return ImageIO.read( resource ) ;
        }
        catch( IOException e ) {
            throw new IllegalStateException( "Unable to load slide resource: " + resourcePath, e ) ;
        }
    }
}
