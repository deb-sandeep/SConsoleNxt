package com.sandy.sconsole.screen.refresher;

import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfig;
import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import lombok.Getter;

public abstract class AbstractRefresherPanel extends Tile {

    // Time in seconds after which this refrsher panel will be rotated.
    @NVPConfig
    @Getter protected int displayDuration = 30*60 ;

    public AbstractRefresherPanel( UITheme theme ) {
        super( theme, false ) ;
        super.setDefaultTableLayout() ;
    }

    public abstract void initialize() ;

    public abstract void refresh() ;

    /**
     * Once this panel gets activated, the refrsher screen will call back
     * the refresherScreenCallback method in the interval returned by
     * this method. A -1 or 0 implies no callback.
     */
    public int getCallbackInterval() { return -1 ; }

    public void refresherScreenCallback() {} ;
}
