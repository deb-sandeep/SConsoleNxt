package com.sandy.sconsole.screen.refresher.slide;

import com.sandy.sconsole.core.SConsoleConfig;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfig;
import com.sandy.sconsole.core.ui.screen.util.ImageTile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.daemon.refresher.RefresherSlideManager;
import com.sandy.sconsole.dao.slide.SlideVO;
import com.sandy.sconsole.screen.refresher.AbstractRefresherPanel;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;

@Slf4j
public class SlideRefresherPanel extends AbstractRefresherPanel {

    private final ImageTile imgTile ;
    private final SConsoleConfig appCfg;
    private final RefresherSlideManager slideManager ;

    @NVPConfig
    private int slideChangeInterval = 300 ;

    public SlideRefresherPanel( UITheme uiTheme, SConsoleConfig cfg,
                                RefresherSlideManager slideManager ) {
        super( uiTheme ) ;

        this.imgTile = new ImageTile( theme ) ;
        this.appCfg = cfg ;
        this.slideManager = slideManager ;
    }

    @Override
    public void initialize() {
        super.setLayout( new BorderLayout() ) ;
        super.add( imgTile, BorderLayout.CENTER ) ;
    }

    @Override
    public void refresh() {
        loadNextSlide() ;
    }

    @Override
    public void refresherScreenCallback() {
        loadNextSlide() ;
    }

    @Override
    public int getCallbackInterval() {
        return this.slideChangeInterval ;
    }

    private void loadNextSlide() {

        try {
            SlideVO slide = slideManager.getNextSlide() ;
            log.debug( "  Setting new slide - {}", slide.getSlideName() ) ;

            BufferedImage bufferedImage = slide.getImage() ;
            imgTile.setImage( slide.getImage() ) ;
            // Remove the image so that there is no excessive memory
            // build up.
            slide.setImage( null ) ;
        }
        catch( Exception e ) {
            log.error( "Could not load slide.", e ) ;
        }
    }
}
