package com.sandy.sconsole.core.ui.screen.tiles;

import com.sandy.sconsole.core.ui.screen.Tile;

import java.awt.*;

public class DebugTile extends Tile {
    
    public DebugTile() {
        this( Color.DARK_GRAY ) ;
    }

    public DebugTile( Color color ) {
        super( true ) ;
        super.setBackground( color ) ;
    }
}
