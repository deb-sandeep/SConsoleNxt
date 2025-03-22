package com.sandy.sconsole.ui.screen.session;

import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.ui.screen.dashboard.tile.daygantt.DayGanttTile;
import com.sandy.sconsole.ui.util.DateTile;
import com.sandy.sconsole.ui.util.TimeTile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SessionScreen extends Screen {
    
    public static final String ID = "SESSION_SCREEN" ;
    
    @Autowired private UITheme theme ;

    private final DateTile dateTile ;
    private final TimeTile timeTile ;
    
    @Autowired private DayGanttTile dayGanttTile ;
    
    public SessionScreen() {
        super( ID, "Session Screen" ) ;
        super.asPerpetual() ;
        this.dateTile = new DateTile( 40, "dd MMM, EEE" ) ;
        this.timeTile = new TimeTile( 50 ) ;
    }
    
    @Override
    public void initialize() {
        super.setUpBaseUI( theme ) ;
        setUpUI() ;
    }
    
    private void setUpUI() {
        addTile( dayGanttTile, 0,  0, 15, 1 ) ;
        addTile( dateTile,     0,  2,  2, 4 ) ;
        addTile( timeTile,     13, 2, 15, 4 ) ;
    }
}
