package com.sandy.sconsole.ui.screen.session.tile.center.face;

import com.sandy.sconsole.core.ui.screen.Tile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
@Scope( "prototype" )
public class TheoryTileFace extends Tile {

    private static final Color DEBUG_BG_COLOR = new Color( 0x1C, 0x24, 0x38 ) ;

    public TheoryTileFace() {
        setBackground( DEBUG_BG_COLOR ) ;
    }
}
