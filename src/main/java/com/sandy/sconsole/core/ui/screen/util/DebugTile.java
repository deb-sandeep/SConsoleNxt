package com.sandy.sconsole.core.ui.screen.util;

import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.core.ui.uiutil.DefaultUITheme;

import java.awt.*;

public class DebugTile extends Tile {

    public DebugTile( Screen parent, Color color ) {
        super( parent, new DefaultUITheme(), true ) ;
        super.setBackground( color ) ;
    }
}
