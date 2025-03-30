package com.sandy.sconsole.ui.screen.dashboard.tile.burn;

import com.sandy.sconsole.core.ui.uiutil.SwingUtils;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.state.ActiveTopicStatistics;
import com.sandy.sconsole.ui.util.ConfiguredUIAttributes;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.sandy.sconsole.core.ui.uiutil.SwingUtils.createEmptyLabel;

@Component
@Scope( "prototype" )
public class TopicBurnPanel extends JPanel {
    
    @Autowired private ConfiguredUIAttributes uiAttributes ;
    @Autowired private UITheme theme ;
    @Autowired private BurnMeterCanvas burnMeter ;
    @Autowired private PctCompletionCanvas pctCompletionBar ;

    private ActiveTopicStatistics topicStats ;
    
    private JLabel topicNameLabel ;
    private JLabel originalBurnLabel ;
    private JLabel requiredBurnLabel ;
    
    TopicBurnPanel() {}
    
    @PostConstruct
    private void setUpUI() {
        super.setLayout( new BorderLayout() ) ;
        super.setBackground( UITheme.BG_COLOR ) ;
        super.setBorder( new EmptyBorder( 0, 10, 0, 10 ) ) ;
        
        topicNameLabel = createEmptyLabel( theme ) ;
        topicNameLabel.setHorizontalAlignment( SwingConstants.LEFT ) ;
        topicNameLabel.setFont( UITheme.BASE_FONT.deriveFont( Font.PLAIN, 25f ) ) ;
        
        originalBurnLabel = createEmptyLabel( theme ) ;
        originalBurnLabel.setHorizontalAlignment( SwingConstants.RIGHT ) ;
        originalBurnLabel.setFont( UITheme.BASE_FONT.deriveFont( Font.PLAIN, 18f ) ) ;
        originalBurnLabel.setForeground( BurnMeterCanvas.ORIGINAL_BURN_COLOR );
        
        requiredBurnLabel = createEmptyLabel( theme ) ;
        requiredBurnLabel.setHorizontalAlignment( SwingConstants.RIGHT ) ;
        requiredBurnLabel.setFont( UITheme.BASE_FONT.deriveFont( Font.PLAIN, 18f ) ) ;
        requiredBurnLabel.setForeground( BurnMeterCanvas.TARGET_BURN_COLOR ) ;
        
        add( getTopicNamePanel(), BorderLayout.NORTH ) ;
        
        JPanel centerPanel = new JPanel( new BorderLayout() ) ;
        centerPanel.setBackground( UITheme.BG_COLOR ) ;
        centerPanel.add( pctCompletionBar, BorderLayout.NORTH ) ;
        centerPanel.add( burnMeter, BorderLayout.CENTER ) ;
        add( centerPanel, BorderLayout.CENTER ) ;
    }
    
    public void setTopicStats( ActiveTopicStatistics topicStats ) {
        this.topicStats = topicStats ;
        this.burnMeter.setTopicStats( topicStats ) ;
        this.pctCompletionBar.setTopicStats( topicStats ) ;
    }
    
    private JPanel getTopicNamePanel() {
        
        JPanel markerPanel = new JPanel( new GridLayout(1, 2, 5, 0) ) ;
        markerPanel.setBackground( UITheme.BG_COLOR ) ;
        markerPanel.add( originalBurnLabel, 0 ) ;
        markerPanel.add( requiredBurnLabel, 1 ) ;
        
        JPanel panel = new JPanel( new BorderLayout() ) ;
        panel.setBackground( UITheme.BG_COLOR ) ;
        panel.add( topicNameLabel, BorderLayout.CENTER ) ;
        panel.add( markerPanel, BorderLayout.EAST ) ;
        
        return panel ;
    }
    
    void refreshUI() {
        
        topicNameLabel.setText( "" ) ;
        
        burnMeter.refreshUI() ;
        pctCompletionBar.refreshUI() ;

        if( topicStats != null ) {
            String syllabusName = topicStats.getTopic().getSyllabusName() ;
            Color syllabusColor = uiAttributes.getSyllabusColor( syllabusName ) ;
            
            topicNameLabel.setForeground( SwingUtils.darkerColor( syllabusColor, 0.6F ) ) ;
            topicNameLabel.setText( topicStats.getTopic().getTopicName() ) ;
            
            originalBurnLabel.setText( "[" + burnMeter.getOriginalBurnRate() + "]" ) ;
            requiredBurnLabel.setText( "[" + burnMeter.getRequiredBurnRate() + "]" ) ;
        }
    }
}
