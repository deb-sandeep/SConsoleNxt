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
import java.awt.*;

import static com.sandy.sconsole.core.ui.uiutil.SwingUtils.createEmptyLabel;

@Component
@Scope( "prototype" )
public class TopicBurnPanel extends JPanel {
    
    private final Font TITLE_NUM_FONT = UITheme.BASE_FONT.deriveFont( Font.PLAIN, 20f ) ;
    
    private class PigeonPanel extends JPanel {
        
        private final JLabel leftBracketLabel  = createEmptyLabel( theme, TITLE_NUM_FONT ) ;
        private final JLabel rightBracketLabel = createEmptyLabel( theme, TITLE_NUM_FONT ) ;
        private final JLabel numPigeonsLabel   = createEmptyLabel( theme, TITLE_NUM_FONT ) ;
        
        PigeonPanel() {
            
            numPigeonsLabel.setHorizontalAlignment( SwingConstants.RIGHT ) ;
            
            numPigeonsLabel.setForeground( Color.ORANGE ) ;
            rightBracketLabel.setForeground( Color.ORANGE );
            leftBracketLabel.setForeground( Color.ORANGE );
            
            setLayout( new BorderLayout() ) ;
            
            add( leftBracketLabel, BorderLayout.WEST ) ;
            add( rightBracketLabel, BorderLayout.EAST ) ;
            add( numPigeonsLabel, BorderLayout.CENTER ) ;
        }
        
        public void setNumPigeons( int numPigeonedProblems ) {
            if( numPigeonedProblems <= 0 ) {
                leftBracketLabel.setText( "" ) ;
                rightBracketLabel.setText( "" ) ;
                numPigeonsLabel.setText( "" ) ;
                numPigeonsLabel.setIcon( null ) ;
            }
            else {
                leftBracketLabel.setText( "[" ) ;
                rightBracketLabel.setText( "]" ) ;
                
                numPigeonsLabel.setText( Integer.toString( numPigeonedProblems ) ) ;
                numPigeonsLabel.setIcon( SwingUtils.getIcon( "pigeon-small.png" ) ) ;
            }
        }
    }
    
    @Autowired private ConfiguredUIAttributes uiAttributes ;
    @Autowired private UITheme theme ;
    @Autowired private BurnMeterCanvas burnMeter ;
    @Autowired private PctCompletionCanvas pctCompletionBar ;

    private ActiveTopicStatistics topicStats ;
    
    private JLabel topicNameLabel ;
    private JLabel originalBurnLabel ;
    private JLabel requiredBurnLabel ;
    private PigeonPanel pigeonPanel ;
    
    TopicBurnPanel() {}
    
    @PostConstruct
    private void setUpUI() {
        super.setLayout( new BorderLayout() ) ;
        super.setBackground( UITheme.BG_COLOR ) ;
        
        topicNameLabel = createEmptyLabel( theme ) ;
        topicNameLabel.setHorizontalAlignment( SwingConstants.LEFT ) ;
        topicNameLabel.setFont( UITheme.BASE_FONT.deriveFont( Font.PLAIN, 25f ) ) ;
        
        originalBurnLabel = createEmptyLabel( theme, TITLE_NUM_FONT ) ;
        originalBurnLabel.setHorizontalAlignment( SwingConstants.RIGHT ) ;
        originalBurnLabel.setVerticalAlignment( SwingConstants.CENTER );
        originalBurnLabel.setForeground( BurnMeterCanvas.ORIGINAL_BURN_COLOR );
        
        requiredBurnLabel = createEmptyLabel( theme, TITLE_NUM_FONT ) ;
        requiredBurnLabel.setHorizontalAlignment( SwingConstants.RIGHT ) ;
        requiredBurnLabel.setVerticalAlignment( SwingConstants.CENTER );
        requiredBurnLabel.setForeground( BurnMeterCanvas.TARGET_BURN_COLOR ) ;
        
        pigeonPanel = new PigeonPanel() ;
        
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
        
        if( topicStats == null ) {
            this.pigeonPanel.setNumPigeons( 0 ) ;
        }
        else {
            this.pigeonPanel.setNumPigeons( topicStats.getNumPigeonedProblems() );
        }
    }
    
    private JPanel getTopicNamePanel() {
        
        JPanel markerPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT, 5, 0 ) ) ;
        
        markerPanel.setPreferredSize( new Dimension( 200, 10 ) ) ;
        markerPanel.setBackground( UITheme.BG_COLOR ) ;
        markerPanel.add( pigeonPanel, 0 ) ;
        markerPanel.add( originalBurnLabel, 1 ) ;
        markerPanel.add( requiredBurnLabel, 2 ) ;
        
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
            
            pigeonPanel.setNumPigeons( topicStats.getNumPigeonedProblems() ) ;
        }
        else {
            pigeonPanel.setNumPigeons( 0 ) ;
            originalBurnLabel.setText( "" ) ;
            requiredBurnLabel.setText( "" ) ;
        }
    }
}
