package com.sandy.sconsole.ui.screen.session.tile.center.face;

import com.sandy.sconsole.core.ui.screen.Tile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
@Scope( "prototype" )
public class ExerciseTileFace extends Tile {

    private static final Color DEBUG_BG_COLOR = new Color( 0x3A, 0x24, 0x16 ) ;

    public ExerciseTileFace() {
        setBackground( DEBUG_BG_COLOR ) ;
    }
}
