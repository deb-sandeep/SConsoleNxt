package com.sandy.sconsole.ui.screen.dashboard.tile.burn;

import com.sandy.sconsole.core.ui.uiutil.SwingUtils;
import com.sandy.sconsole.core.ui.uiutil.UIConstant;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.ui.util.ActiveTopicStatistics;
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
    
    TopicBurnPanel() {}
    
    @PostConstruct
    private void setUpUI() {
        super.setLayout( new BorderLayout() ) ;
        super.setBackground( theme.getBackgroundColor() ) ;
        super.setBorder( new EmptyBorder( 0, 10, 0, 10 ) ) ;
        
        topicNameLabel = createEmptyLabel( theme ) ;
        topicNameLabel.setHorizontalAlignment( SwingConstants.LEFT ) ;
        topicNameLabel.setFont( UIConstant.BASE_FONT.deriveFont( Font.PLAIN, 22f ) ) ;
        
        add( topicNameLabel, BorderLayout.NORTH ) ;
        
        JPanel centerPanel = new JPanel( new BorderLayout() ) ;
        centerPanel.setBackground( theme.getBackgroundColor() ) ;
        centerPanel.add( pctCompletionBar, BorderLayout.NORTH ) ;
        centerPanel.add( burnMeter, BorderLayout.CENTER ) ;
        add( centerPanel, BorderLayout.CENTER ) ;
    }
    
    public void setTopicStats( ActiveTopicStatistics topicStats ) {
        this.topicStats = topicStats ;
        this.burnMeter.setTopicStats( topicStats ) ;
        this.pctCompletionBar.setTopicStats( topicStats ) ;
    }
    
    void refreshUI() {
        
        topicNameLabel.setText( "" ) ;
        if( topicStats != null ) {
            String syllabusName = topicStats.getTopic().getSyllabusName() ;
            Color syllabusColor = uiAttributes.getSyllabusColor( syllabusName ) ;
            
            topicNameLabel.setForeground( SwingUtils.darkerColor( syllabusColor, 0.6F ) ) ;
            topicNameLabel.setText( topicStats.getTopic().getTopicName() ) ;
        }
        burnMeter.refreshUI() ;
        pctCompletionBar.refreshUI() ;
    }
}
