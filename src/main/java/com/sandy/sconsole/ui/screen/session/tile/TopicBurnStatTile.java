package com.sandy.sconsole.ui.screen.session.tile;

import com.sandy.sconsole.EventCatalog;
import com.sandy.sconsole.core.bus.Event;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.bus.EventSubscriber;
import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.core.util.StringUtil;
import com.sandy.sconsole.dao.session.dto.SessionDTO;
import com.sandy.sconsole.state.ActiveTopicStatistics;
import com.sandy.sconsole.state.manager.ActiveTopicStatisticsManager;
import com.sandy.sconsole.state.manager.TodayStudyStatistics;
import info.clearthought.layout.TableLayout;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

import static javax.swing.SwingConstants.CENTER;

@Slf4j
@Component
@Scope( "prototype" )
public class TopicBurnStatTile extends Tile
    implements EventSubscriber {
    
    private static final SimpleDateFormat DF = new SimpleDateFormat( "dd-MMM" ) ;
    
    private static final Font LBL_FONT    = UITheme.BASE_FONT.deriveFont( 20F ) ;
    private static final Font VAL_FONT    = UITheme.BASE_FONT.deriveFont( 23F ) ;
    
    private static final int[] SUBSCRIBED_EVENTS = {
            EventCatalog.ATS_REFRESHED,
            EventCatalog.SESSION_ENDED
    } ;
    
    private final JLabel burnEndDtLbl        = createDefaultLabel( "Burn end" ) ;
    private final JLabel burnEndDt           = createDefaultLabel( "" ) ;
    
    private final JLabel burnStartDtLbl      = createDefaultLabel( "Burn start" ) ;
    private final JLabel burnStartDt         = createDefaultLabel( "" ) ;
    
    private final JLabel numQLbl             = createDefaultLabel( "Num Q" ) ;
    private final JLabel numQ                = createDefaultLabel( "" ) ;
    
    private final JLabel numSolvedQLbl       = createDefaultLabel( "Num Q solved" ) ;
    private final JLabel numSolvedQ          = createDefaultLabel( "" ) ;
    
    private final JLabel numRemainingQLbl    = createDefaultLabel( "Num Q remaining" ) ;
    private final JLabel numRemainingQ       = createDefaultLabel( "" ) ;
    
    private final JLabel baselineBurnRateLbl = createDefaultLabel( "Baseline burn rate" ) ;
    private final JLabel baselineBurnRate    = createDefaultLabel( "" ) ;
    
    private final JLabel currentBurnRateLbl  = createDefaultLabel( "Current burn rate" ) ;
    private final JLabel currentBurnRate     = createDefaultLabel( "" ) ;
    
    private final JLabel requiredBurnRateLbl = createDefaultLabel( "Required burn rate" ) ;
    private final JLabel requiredBurnRate    = createDefaultLabel( "" ) ;
    
    private final JLabel overshootDaysLbl    = createDefaultLabel( "Overshoot days" ) ;
    private final JLabel overshootDays       = createDefaultLabel( "" ) ;
    
    private final JLabel[] labels = {
            burnStartDtLbl,
            burnStartDt,
            burnEndDtLbl,
            burnEndDt,
            numQLbl,
            numQ,
            numSolvedQLbl,
            numSolvedQ,
            numRemainingQLbl,
            numRemainingQ,
            baselineBurnRateLbl,
            baselineBurnRate,
            currentBurnRateLbl,
            currentBurnRate,
            requiredBurnRateLbl,
            requiredBurnRate,
            overshootDaysLbl,
            overshootDays
    } ;
    
    private ActiveTopicStatistics ats ;
    
    @Autowired private EventBus eventBus ;
    @Autowired private ActiveTopicStatisticsManager atsManager ;
    @Autowired private TodayStudyStatistics todayStudyStats ;

    public TopicBurnStatTile() {
        setUpUI() ;
    }
    
    private JLabel createDefaultLabel( String defaultText ) {
        
        JLabel label = new JLabel() ;
        label.setVerticalAlignment( CENTER ) ;
        label.setOpaque( true ) ;
        label.setBackground( UITheme.BG_COLOR ) ;
        
        if( StringUtil.isNotEmptyOrNull( defaultText ) ) {
            label.setText( defaultText ) ;
            label.setForeground( Color.GRAY.darker() ) ;
            label.setFont( LBL_FONT ) ;
            label.setBorder( BorderFactory.createEmptyBorder( 0, 10, 0, 0 ) );
            label.setHorizontalAlignment( SwingConstants.LEFT ) ;
        }
        else {
            label.setText( "" ) ;
            label.setForeground( Color.GRAY.brighter() ) ;
            label.setFont( VAL_FONT ) ;
            label.setBorder( BorderFactory.createEmptyBorder( 0, 0, 0, 10 ) );
            label.setHorizontalAlignment( SwingConstants.RIGHT ) ;
        }
        return label ;
    }
    
    private void setUpUI() {
        
        setLayout() ;
        
        for( int row=0; row<labels.length/2; row++ ) {
            JLabel titleLbl = labels[2*row] ;
            JLabel valLbl   = labels[2*row+1] ;
            
            add( titleLbl, "0," + row + ",0," + row ) ;
            add( valLbl,   "1," + row + ",1," + row ) ;
        }
    }
    
    private void setLayout() {
        
        int numRows = labels.length/2 ;
        float rowHeightPct = 1.0F/numRows ;
        
        TableLayout layout = new TableLayout() ;
        for( int i=0; i<numRows; i++ ) {
            layout.insertRow( i, rowHeightPct ) ;
        }
        
        layout.insertColumn( 0, 0.66F ) ;
        layout.insertColumn( 1, 0.33F ) ;
        setLayout( layout ) ;
    }
    
    @Override
    public void beforeActivation() {
        eventBus.addSubscriber( this, true, SUBSCRIBED_EVENTS ) ;
        
        SessionDTO liveSession = todayStudyStats.getCurrentSession() ;
        ats = atsManager.getTopicStatistics( liveSession.getTopicId() ) ;
        refreshBurnInfo() ;
    }
    
    @Override
    public void beforeDeactivation() {
        eventBus.removeSubscriber( this ) ;
    }
    
    @Override
    public void handleEvent( Event event ) {
        final int eventType = event.getEventId() ;
        switch( eventType ) {
            case EventCatalog.ATS_REFRESHED -> refreshBurnInfo() ;
            case EventCatalog.SESSION_ENDED -> ats = null ;
        }
    }
    
    public void refreshBurnInfo() {
        
        numQ.setText             ( String.valueOf( ats.getNumTotalProblems() ) ) ;
        burnStartDt.setText      ( DF.format( ats.getExerciseStartDate() ) ) ;
        numSolvedQ.setText       ( String.valueOf( ats.numProblemsCompleted() ) ) ;
        numRemainingQ.setText    ( String.valueOf( ats.getNumProblemsLeft() ) ) ;
        baselineBurnRate.setText ( String.valueOf( ats.getOriginalBurnRate() ) ) ;
        
        setBurnEndDateLabel() ;
        setCurrentBurnRateLabel() ;
        setRequiredBurnRateLabel() ;
        setProjectedEndDtLabel() ;
    }

    private void setBurnEndDateLabel() {
        burnEndDt.setText( DF.format( ats.getExerciseEndDate() ) ) ;
        if( ats.getNumExerciseDaysLeft() <= 0 ) {
            burnEndDt.setBackground( Color.RED ) ; 
        }
        else {
            burnEndDt.setBackground( UITheme.BG_COLOR ) ;
        }
    }


    private void setCurrentBurnRateLabel() {
        
        currentBurnRate.setText( String.valueOf( ats.getCurrentBurnRate() ) ) ;
        
        if( ats.getCurrentBurnRate() < ats.getRequiredBurnRate() ) {
            currentBurnRate.setForeground( Color.RED ) ;
        }
        else {
            currentBurnRate.setForeground( Color.GREEN.darker() ) ;
        }
    }
    
    private void setRequiredBurnRateLabel() {
        
        if( ats.getRequiredBurnRate() != 0 ) {
            requiredBurnRate.setText ( String.valueOf( ats.getRequiredBurnRate() ) ) ;
            if( ats.getRequiredBurnRate() <= ats.getOriginalBurnRate() ) {
                requiredBurnRate.setForeground( Color.GREEN.darker() ) ;
            }
            else {
                requiredBurnRate.setForeground( Color.RED ) ;
            }
        }
        else {
            requiredBurnRate.setText( "" ) ;
        }
    }

    private void setProjectedEndDtLabel() {
        
        overshootDays.setText ( String.valueOf( ats.getNumOvershootDays() ) ) ;
        if( ats.getNumOvershootDays() <= 0 ) {
            overshootDays.setForeground( Color.GREEN.darker() ) ;
        }
        else {
            overshootDays.setForeground( Color.RED ) ;
        }
    }
    
}
