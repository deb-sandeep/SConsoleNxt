package com.sandy.sconsole.screen.screens.refresher;

import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;

public class RefresherPanel extends Tile {

    private final RefresherScreen parentScreen ;

    public RefresherPanel( RefresherScreen screen, UITheme theme ) {
        super( theme, false ) ;
        this.parentScreen = screen ;
        super.setDefaultTableLayout() ;
    }

    public void initialize() {}
}
