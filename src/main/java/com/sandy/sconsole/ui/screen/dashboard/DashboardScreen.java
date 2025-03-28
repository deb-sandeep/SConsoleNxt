package com.sandy.sconsole.ui.screen.dashboard ;

import com.sandy.sconsole.core.clock.ClockTickListener;
import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.ui.screen.dashboard.tile.DateTimeTile;
import com.sandy.sconsole.ui.screen.dashboard.tile.SyllabusL30EffortTile;
import com.sandy.sconsole.ui.screen.dashboard.tile.TotalL60EffortTile;
import com.sandy.sconsole.ui.screen.dashboard.tile.burn.SyllabusBurnTile;
import com.sandy.sconsole.ui.screen.dashboard.tile.daygantt.DayGanttTile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.border.MatteBorder;

import static com.sandy.sconsole.AppConstants.*;

@Component
public class DashboardScreen extends Screen
    implements ClockTickListener {
    
    public static final String ID = "DASHBOARD_SCREEN" ;
    
    @Autowired private UITheme theme ;
    
    @Autowired private DayGanttTile dayGanttTile ;
    @Autowired private DateTimeTile dateTimeTile ;
    
    @Autowired private SyllabusBurnTile phyBurnTile ;
    @Autowired private SyllabusBurnTile chemBurnTile ;
    @Autowired private SyllabusBurnTile mathsBurnTile;
    @Autowired private SyllabusBurnTile reasoningBurnTile ;
    
    @Autowired private SyllabusL30EffortTile phyL30StudyTimeTile ;
    @Autowired private SyllabusL30EffortTile chemL30StudyTimeTile ;
    @Autowired private SyllabusL30EffortTile mathsL30StudyTimeTile ;
    @Autowired private SyllabusL30EffortTile reasoningL30StudyTimeTile ;
    
    @Autowired private TotalL60EffortTile l60EffortTile ;
    
    public DashboardScreen() {
        super( ID, "Daily Dashboard" ) ;
    }
    
    @Override
    public void initialize() {
        super.setUpBaseUI( theme ) ;
        
        phyBurnTile.setSyllabusName( IIT_PHY_SYLLABUS_NAME ) ;
        chemBurnTile.setSyllabusName( IIT_CHEM_SYLLABUS_NAME ) ;
        mathsBurnTile.setSyllabusName( IIT_MATHS_SYLLABUS_NAME ) ;
        reasoningBurnTile.setSyllabusName( REASONING_SYLLABUS_NAME ) ;
        
        phyL30StudyTimeTile.setSyllabusName( IIT_PHY_SYLLABUS_NAME ) ;
        chemL30StudyTimeTile.setSyllabusName( IIT_CHEM_SYLLABUS_NAME ) ;
        mathsL30StudyTimeTile.setSyllabusName( IIT_MATHS_SYLLABUS_NAME ) ;
        reasoningL30StudyTimeTile.setSyllabusName( REASONING_SYLLABUS_NAME ) ;
        
        setUpUI() ;
    }
    
    private void setUpUI() {
        setUpTileBorders() ;
        
        super.addTile( dayGanttTile, 0,  0, 15,  1 ) ;
        super.addTile( dateTimeTile, 0,  2, 15,  5 ) ;
        
        super.addTile( phyBurnTile,       0,  6,  7, 10 ) ;
        super.addTile( chemBurnTile,      8,  6, 15, 10 ) ;
        super.addTile( mathsBurnTile,     0, 16,  7, 20 ) ;
        super.addTile( reasoningBurnTile, 8, 16, 15, 20 ) ;
        
        super.addTile( phyL30StudyTimeTile,       0,  11,  7,  15 ) ;
        super.addTile( chemL30StudyTimeTile,      8,  11, 15,  15 ) ;
        super.addTile( mathsL30StudyTimeTile,     0,  21,  7,  25 ) ;
        super.addTile( reasoningL30StudyTimeTile, 8,  21,  15, 25 ) ;
        
        super.addTile( l60EffortTile, 0, 26, 15, 31 ) ;
    }
    
    private void setUpTileBorders() {
        phyL30StudyTimeTile.setBorder( new MatteBorder( 0, 1, 1, 1, UITheme.TILE_BORDER_COLOR ) ) ;
        chemL30StudyTimeTile.setBorder( new MatteBorder( 0, 1, 1, 1, UITheme.TILE_BORDER_COLOR ) ) ;
        mathsL30StudyTimeTile.setBorder( new MatteBorder( 0, 1, 1, 1, UITheme.TILE_BORDER_COLOR ) ) ;
        reasoningL30StudyTimeTile.setBorder( new MatteBorder( 0, 1, 1, 1, UITheme.TILE_BORDER_COLOR ) ) ;
    }
}
