package com.sandy.sconsole.ui.screen.session;

import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.screen.tiles.DebugTile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
public class SessionScreen extends Screen {
    
    public static final String ID = "SESSION_SCREEN" ;
    
    @Autowired private UITheme theme ;
    
    public SessionScreen() {
        super( ID, "Daily Dashboard" ) ;
        super.asPerpetual() ;
    }
    
    @Override
    public void initialize() {
        super.setUpBaseUI( theme ) ;
        setUpUI() ;
    }
    
    private void setUpUI() {
        DebugTile debugTile = new DebugTile( Color.GREEN );
        super.addTile( debugTile, 0, 11, 15, 15 ) ;
    }
}
