package com.sandy.sconsole.ui.screen.session;

import com.sandy.sconsole.EventCatalog;
import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.screen.tiles.ImageTile;
import com.sandy.sconsole.core.ui.screen.tiles.StringTile;
import com.sandy.sconsole.core.ui.uiutil.SwingUtils;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.dao.master.repo.SessionTypeRepo;
import com.sandy.sconsole.dao.master.repo.SyllabusRepo;
import com.sandy.sconsole.dao.session.dto.SessionDTO;
import com.sandy.sconsole.state.manager.TodayStudyStatistics;
import com.sandy.sconsole.ui.screen.dashboard.tile.SyllabusL30EffortTile;
import com.sandy.sconsole.ui.screen.dashboard.tile.daygantt.DayGanttTile;
import com.sandy.sconsole.ui.screen.session.tile.FragmentationTile;
import com.sandy.sconsole.ui.screen.session.tile.ThermometerTile;
import com.sandy.sconsole.ui.screen.session.tile.TopicBurnChartTile;
import com.sandy.sconsole.ui.screen.session.tile.TopicBurnStatTile;
import com.sandy.sconsole.ui.util.ConfiguredUIAttributes;
import com.sandy.sconsole.ui.util.DateTile;
import com.sandy.sconsole.ui.util.TimeTile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.border.MatteBorder;
import java.awt.*;

import static com.sandy.sconsole.core.util.StringUtil.getElapsedTimeLabelHHmm;

@Component
public class SessionScreen extends Screen
    implements EventSubscriber {
    
    public static final String ID = "SESSION_SCREEN" ;
    private static final int[] SUBSCRIBED_EVENTS = {
            EventCatalog.SESSION_STARTED,
            EventCatalog.SESSION_EXTENDED,
            EventCatalog.ATS_REFRESHED
    } ;
    
    @Autowired private SyllabusRepo syllabusRepo ;
    @Autowired private SessionTypeRepo sessionTypeRepo ;
    
    @Autowired private UITheme theme ;
    @Autowired private EventBus eventBus ;
    @Autowired private ConfiguredUIAttributes uiAttributes ;
    @Autowired private TodayStudyStatistics todayStudyStats ;

    private final DateTile dateTile ;
    private final TimeTile timeTile ;
    private final StringTile syllabusTile ;
    private final StringTile sessionTimeTile ;
    private final StringTile topicTile ;
    private final ImageTile sessionTypeIconTile ;
    private final ImageTile syllabusIconTile ;
    
    private Color syllabusColor ;
    
    @Autowired private DayGanttTile dayGanttTile ;
    @Autowired private TopicBurnStatTile topicBurnStatTile ;
    @Autowired private FragmentationTile fragmentationTile ;
    @Autowired private SyllabusL30EffortTile sylL30EffortTile;
    @Autowired private TopicBurnChartTile burnChartTile;
    @Autowired private ThermometerTile thermometerTile ;
    
    private String sessionType ;
    private String syllabusName ;
    private int topicId ;
    private String topicName ;
    
    public SessionScreen() {
        super( ID, "Session Screen" ) ;
        super.asPerpetual() ;
        
        // Initialize only the non autowired tiles
        this.dateTile = new DateTile( 40, "dd MMM, EEE" ) ;
        this.timeTile = new TimeTile( 50 ) ;
        this.syllabusTile = new StringTile( 50 ) ;
        this.topicTile = new StringTile( 40 ) ;
        this.sessionTimeTile = new StringTile( 50 ) ;
        this.sessionTypeIconTile = new ImageTile() ;
        this.syllabusIconTile = new ImageTile() ;
    }
    
    @Override
    public void initialize() {
        super.setUpBaseUI( theme ) ;
        setUpUI() ;
    }
    
    @Override
    public void beforeActivation() {
        eventBus.addSubscriberForEventTypes( this, true, SUBSCRIBED_EVENTS ) ;
        _handleSessionStarted( todayStudyStats.getLiveSession() ) ;
    }
    
    @Override
    public void beforeDeactivation() {
        eventBus.removeSubscriber( this ) ;
    }
    
    private void setUpUI() {
        setUpTileBorders() ;
        
        addTile( dayGanttTile,        0,   0, 15,  1 ) ;
        addTile( dateTile,            0,   2,  2,  4 ) ;
        addTile( sessionTypeIconTile, 3,   2,  3,  4 ) ;
        addTile( syllabusIconTile,    4,   2,  4,  4 ) ;
        addTile( sessionTimeTile,    11,   2, 12,  4 ) ;
        addTile( timeTile,           13,   2, 15,  4 ) ;
        addTile( syllabusTile,        5,   2, 10,  4 ) ;
        addTile( topicTile,           3,   5, 12,  6 ) ;
        addTile( topicBurnStatTile,   0,   5,  2, 17 ) ;
        addTile( fragmentationTile,   13,  5, 15, 17 ) ;
        addTile( sylL30EffortTile,     9, 18, 15, 24 ) ;
        addTile( burnChartTile,        0, 18,  7, 31 ) ;
        addTile( thermometerTile,      8, 18,  8, 31 ) ;
    }
    
    private void setUpTileBorders() {
        this.dateTile.enableBorder( true ) ;
        this.timeTile.enableBorder( true ) ;
        this.sylL30EffortTile.enableBorder( true ) ;
        this.sessionTypeIconTile.setBorder( new MatteBorder( 1, 0, 1, 0, UITheme.TILE_BORDER_COLOR ) );
        this.syllabusIconTile.setBorder( new MatteBorder( 1, 0, 1, 0, UITheme.TILE_BORDER_COLOR ) ) ;
        this.syllabusTile.setBorder( new MatteBorder( 1, 0, 1, 0, UITheme.TILE_BORDER_COLOR ) ) ;
        this.sessionTimeTile.setBorder( new MatteBorder( 1, 0, 1, 0, UITheme.TILE_BORDER_COLOR ) ) ;
        this.topicBurnStatTile.setBorder( new MatteBorder( 0, 1, 0, 1, UITheme.TILE_BORDER_COLOR ) );
        this.burnChartTile.setBorder( new MatteBorder( 1, 1, 1, 1, UITheme.TILE_BORDER_COLOR ) );
        this.thermometerTile.setBorder( new MatteBorder( 1, 0, 1, 0, UITheme.TILE_BORDER_COLOR ) );
    }

    @Override
    public void handleEvent( Event event ) {
        final int eventType = event.getEventType() ;
        switch( eventType ) {
            case EventCatalog.SESSION_STARTED -> _handleSessionStarted( ( SessionDTO )event.getValue() ) ;
            case EventCatalog.SESSION_EXTENDED -> _handleSessionExtended( ( SessionDTO )event.getValue() ) ;
            case EventCatalog.ATS_REFRESHED -> _handleATSRefreshed( (Integer)event.getValue() ) ;
        }
    }
    
    private void _handleSessionStarted( SessionDTO session ) {
        if( session != null ) {
            this.sessionType = session.getSessionType() ;
            this.syllabusName = session.getSyllabusName() ;
            this.topicId = session.getTopicId() ;
            this.topicName = session.getTopicName() ;
            
            this.syllabusColor = uiAttributes.getSyllabusColor( this.syllabusName ) ;
            this.fragmentationTile.setSyllabusName( this.syllabusName ) ;
            this.sylL30EffortTile.setSyllabusName( this.syllabusName ) ;
            this.burnChartTile.setTopicId( this.topicId ) ;
            this.thermometerTile.setTopicId( this.topicId ) ;
            
            setTileForegroundToSyllabusColor() ;
            setSyllabusAndTopicNames() ;
            setSyllabusAndSessionTypeIcons() ;
            refreshSessionTimeTile( session.getEffectiveDuration() ) ;
        }
    }
    
    private void setTileForegroundToSyllabusColor() {
        
        Color fgColor = SwingUtils.darkerColor( syllabusColor, 0.5F ) ;
        this.syllabusTile.setLabelForeground( fgColor ) ;
        this.topicTile.setLabelForeground( fgColor ) ;
        this.sessionTimeTile.setLabelForeground( fgColor ) ;
    }
    
    private void setSyllabusAndTopicNames() {
        this.syllabusTile.setLabelText( syllabusName ) ;
        this.topicTile.setLabelText( topicName ) ;
    }
    
    private void setSyllabusAndSessionTypeIcons() {
        String sylIconName = syllabusRepo.findById( syllabusName ).get().getIconName() ;
        String stIconName = sessionTypeRepo.findBySessionType( sessionType ).getIconName() ;
        
        this.sessionTypeIconTile.setImage( SwingUtils.getIconImage( stIconName ) );
        this.syllabusIconTile.setImage( SwingUtils.getIconImage( sylIconName ) );
    }
    
    private void _handleATSRefreshed( Integer topicId ) {
        assert topicId == this.topicId ;
    }
    
    private void _handleSessionExtended( SessionDTO session ) {
        refreshSessionTimeTile( session.getEffectiveDuration() ) ;
    }
    
    private void refreshSessionTimeTile( int effectiveDuration ) {
        sessionTimeTile.setLabelText( getElapsedTimeLabelHHmm( effectiveDuration ) ) ;
    }
}
