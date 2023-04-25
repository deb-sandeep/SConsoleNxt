package com.sandy.sconsole.core.ui.uiutil;

import com.sandy.sconsole.core.ui.Screen;
import com.sandy.sconsole.core.ui.Tile;

import java.awt.*;

public class DebugTile extends Tile {

    public DebugTile( Screen parent, Color color ) {
        super( parent, new DefaultUITheme(), true ) ;
        super.setBackground( color ) ;
    }
}
