package com.sandy.sconsole.screen.refresher.slide;

import com.sandy.sconsole.core.SConsoleConfig;
import com.sandy.sconsole.core.ui.screen.util.ImageTile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.screen.refresher.AbstractRefresherPanel;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.File;

@Slf4j
public class SlideRefresherPanel extends AbstractRefresherPanel {

    private final ImageTile imgTile ;
    private final SConsoleConfig appCfg;

    public SlideRefresherPanel( UITheme uiTheme, SConsoleConfig cfg ) {
        super( uiTheme ) ;

        this.imgTile = new ImageTile( theme ) ;
        this.appCfg = cfg ;
    }

    @Override
    public void initialize() {
        log.debug( "- Iniitalizing slide refresher panel." ) ;
        super.setLayout( new BorderLayout() ) ;
        super.add( imgTile, BorderLayout.CENTER ) ;
    }

    @Override
    public void refresh() {
        loadNextSlide() ;
    }

    private void loadNextSlide() {
        File file = new File( appCfg.getWorkspacePath(),
                "Refreshers/Class-9/Physics/07 - Reflection of Light/01 - Mirror Formula.png" ) ;
        imgTile.setImage( file ) ;
    }
}
