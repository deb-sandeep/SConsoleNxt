package com.sandy.sconsole.core.ui.screen.util;

import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import lombok.Getter;
import lombok.Setter;

public class StarTile extends Tile {

    @Getter @Setter private float rating ;

    public StarTile( UITheme theme ) {
        super( theme, false ) ;
        setUpUI( theme ) ;
    }

    private void setUpUI( UITheme theme ) {
    }
}
