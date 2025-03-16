package com.sandy.sconsole.ui.screen.dashboard ;

import com.sandy.sconsole.core.clock.ClockTickListener;
import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.ui.screen.dashboard.tile.DateTimeTile;
import com.sandy.sconsole.ui.screen.dashboard.tile.burn.SyllabusBurnTile;
import com.sandy.sconsole.ui.tile.daygantt.DayGanttTile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.sandy.sconsole.ui.util.UIConstants.*;

@Component
public class DashboardScreen extends Screen
    implements ClockTickListener {
    
    public static final String ID = "DASHBOARD_SCREEN" ;
    
    @Autowired private UITheme theme ;
    
    @Autowired private DayGanttTile     dayGanttTile ;
    @Autowired private DateTimeTile     dateTimeTile ;
    @Autowired private SyllabusBurnTile phyBurnTile ;
    @Autowired private SyllabusBurnTile chemBurnTile ;
    @Autowired private SyllabusBurnTile mathBurnTile ;
    @Autowired private SyllabusBurnTile reasoningBurnTile ;
    
    public DashboardScreen() {
        super( ID, "Daily Dashboard" ) ;
    }
    
    @Override
    public void initialize() {
        super.setUpBaseUI( theme ) ;
        phyBurnTile.setSyllabusName( IIT_PHY_SYLLABUS_NAME ) ;
        chemBurnTile.setSyllabusName( IIT_CHEM_SYLLABUS_NAME ) ;
        mathBurnTile.setSyllabusName( IIT_MATHS_SYLLABUS_NAME ) ;
        reasoningBurnTile.setSyllabusName( REASONING_SYLLABUS_NAME ) ;
        
        setUpUI() ;
    }
    
    private void setUpUI() {
        super.addTile( dayGanttTile,      0,  0, 15,  1 ) ;
        super.addTile( dateTimeTile,      0,  2, 15,  5 ) ;
        super.addTile( phyBurnTile,       0,  6,  7, 10 ) ;
        super.addTile( chemBurnTile,      8,  6, 15, 10 ) ;
        super.addTile( mathBurnTile,      0, 16,  7, 20 ) ;
        super.addTile( reasoningBurnTile, 8, 16, 15, 20 ) ;
    }
}
