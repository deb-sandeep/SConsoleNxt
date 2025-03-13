package com.sandy.sconsole.ui.screen.dashboard ;

import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.screen.tiles.DebugTile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DashboardScreen extends Screen {
    public static final String ID = "DASHBOARD_SCREEN" ;
    
    @Autowired private UITheme theme ;
    
    public DashboardScreen() {
        super( ID, "Daily Dashboard" ) ;
    }
    
    @Override
    public void initialize() {
        super.setUpBaseUI( theme ) ;
        setUpUI( theme ) ;
    }
    
    private void setUpUI( UITheme theme ) {
        DebugTile debugTile = new DebugTile() ;

        super.addTile( debugTile, 2, 5, 13, 8 ) ;
    }
}
