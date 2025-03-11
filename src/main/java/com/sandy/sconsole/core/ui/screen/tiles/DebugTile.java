package com.sandy.sconsole.core.ui.screen.tiles;

import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.core.ui.uiutil.DefaultUITheme;

import java.awt.*;

public class DebugTile extends Tile {

    public DebugTile( Color color ) {
        super( new DefaultUITheme(), true ) ;
        super.setBackground( color ) ;
    }
}
