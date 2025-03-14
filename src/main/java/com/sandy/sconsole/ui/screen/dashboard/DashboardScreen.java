package com.sandy.sconsole.ui.screen.dashboard ;

import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.ui.screen.dashboard.tile.DateTimeTile;
import com.sandy.sconsole.ui.tile.daygantt.DayGanttTile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DashboardScreen extends Screen {
    
    public static final String ID = "DASHBOARD_SCREEN" ;
    
    @Autowired private UITheme theme ;
    
    @Autowired private DayGanttTile dayGanttTile ;
    @Autowired private DateTimeTile dateTimeTile ;
    
    public DashboardScreen() {
        super( ID, "Daily Dashboard" ) ;
    }
    
    @Override
    public void initialize() {
        super.setUpBaseUI( theme ) ;
        setUpUI( theme ) ;
    }
    
    private void setUpUI( UITheme theme ) {
        
        super.addTile( dayGanttTile, 0, 0, 15, 0 ) ;
        super.addTile( dateTimeTile, 0, 1, 15, 2 ) ;
    }
}
