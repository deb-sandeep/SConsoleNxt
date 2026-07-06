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
    
    private class MiniZoneBar extends JPanel {
        private static final int DOT_SIZE = 5 ;
        private double fraction = 0.5 ;

        MiniZoneBar() {
            setOpaque( false ) ;
            setPreferredSize( new Dimension( 0, 6 ) ) ;
        }

        void setFraction( double f ) {
            this.fraction = Math.max( 0.0, Math.min( 1.0, f ) ) ;
            repaint() ;
        }

        @Override
        protected void paintComponent( Graphics g ) {
            super.paintComponent( g ) ;
            int w = getWidth(), h = getHeight() ;
            if( w <= 0 || h <= 0 ) return ;
            Graphics2D g2 = (Graphics2D) g.create() ;
            g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON ) ;
            int ly = h / 2 ;
            g2.setColor( Color.DARK_GRAY ) ;
            g2.drawLine( 0, ly, w - 1, ly ) ;
            int cx = Math.max( 0, Math.min( w - DOT_SIZE,
                        (int) Math.round( fraction * w ) - DOT_SIZE / 2 ) ) ;
            g2.setColor( zoneFractionColor( fraction ) ) ;
            g2.fillRect( cx, ly - DOT_SIZE / 2, DOT_SIZE*2, DOT_SIZE ); ;
            g2.dispose() ;
        }
    }

    private class ZoneLabelPanel extends JPanel {
        private final MiniZoneBar miniZoneBar = new MiniZoneBar() ;

        ZoneLabelPanel() {
            setLayout( new BorderLayout() ) ;
            setBackground( UITheme.BG_COLOR ) ;
            add( miniZoneBar, BorderLayout.NORTH ) ;
            add( leadLagLabel, BorderLayout.CENTER ) ;
        }

        void setScore( double score ) {
            int    zi   = ActiveTopicStatistics.zoneIndexFor( score ) ;
            double lo   = ActiveTopicStatistics.ZONE_BOUNDS[ zi ] ;
            double hi   = ActiveTopicStatistics.ZONE_BOUNDS[ zi + 1 ] ;
            double span = hi - lo ;
            miniZoneBar.setFraction( span > 0 ? (score - lo) / span : 0.5 ) ;
        }
    }

    @Autowired private ConfiguredUIAttributes uiAttributes ;
    @Autowired private UITheme theme ;
    @Autowired private BurnMeterCanvas burnMeter ;
    @Autowired private PctCompletionCanvas pctCompletionBar ;

    private ActiveTopicStatistics topicStats ;

    private JLabel topicNameLabel ;
    private JLabel leadLagLabel ;
    private ZoneLabelPanel zoneLabelPanel ;
    private JLabel currentBurnLabel;
    private JLabel requiredBurnLabel ;
    private PigeonPanel pigeonPanel ;
    private JLabel overshootLabel ;
    
    TopicBurnPanel() {}
    
    @PostConstruct
    private void setUpUI() {
        super.setLayout( new BorderLayout() ) ;
        super.setBackground( UITheme.BG_COLOR ) ;
        
        topicNameLabel = createEmptyLabel( theme ) ;
        topicNameLabel.setHorizontalAlignment( SwingConstants.LEFT ) ;
        topicNameLabel.setFont( UITheme.BASE_FONT.deriveFont( Font.PLAIN, 25f ) ) ;

        leadLagLabel = createEmptyLabel( theme ) ;
        leadLagLabel.setFont( UITheme.BASE_FONT.deriveFont( Font.PLAIN, 20f ) ) ;
        leadLagLabel.setVerticalAlignment( SwingConstants.CENTER ) ;
        zoneLabelPanel = new ZoneLabelPanel() ;

        currentBurnLabel = createEmptyLabel( theme, TITLE_NUM_FONT ) ;
        currentBurnLabel.setHorizontalAlignment( SwingConstants.RIGHT ) ;
        currentBurnLabel.setVerticalAlignment( SwingConstants.CENTER );
        currentBurnLabel.setForeground( BurnMeterCanvas.CURRENT_BURN_COLOR );
        
        requiredBurnLabel = createEmptyLabel( theme, TITLE_NUM_FONT ) ;
        requiredBurnLabel.setHorizontalAlignment( SwingConstants.RIGHT ) ;
        requiredBurnLabel.setVerticalAlignment( SwingConstants.CENTER );
        
        pigeonPanel = new PigeonPanel() ;
        
        overshootLabel = createEmptyLabel( theme, TITLE_NUM_FONT ) ;
        overshootLabel.setHorizontalAlignment( SwingConstants.RIGHT ) ;
        overshootLabel.setVerticalAlignment( SwingConstants.CENTER ) ;
        
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
        
        JPanel markerPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT, 10, 0 ) ) ;
        
        markerPanel.setPreferredSize( new Dimension( 250, 10 ) ) ;
        markerPanel.setBackground( UITheme.BG_COLOR ) ;
        markerPanel.add( pigeonPanel, 0 ) ;
        markerPanel.add( currentBurnLabel, 1 ) ;
        markerPanel.add( requiredBurnLabel, 2 ) ;
        markerPanel.add( overshootLabel, 3 ) ;
        
        JPanel nameAndIndicatorPanel = new JPanel( new FlowLayout( FlowLayout.LEFT, 8, 0 ) ) ;
        nameAndIndicatorPanel.setBackground( UITheme.BG_COLOR ) ;
        nameAndIndicatorPanel.add( topicNameLabel ) ;
        nameAndIndicatorPanel.add( zoneLabelPanel ) ;

        JPanel panel = new JPanel( new BorderLayout() ) ;
        panel.setBackground( UITheme.BG_COLOR ) ;
        panel.add( nameAndIndicatorPanel, BorderLayout.CENTER ) ;
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

            leadLagLabel.setText( "[" + topicStats.getScoreLabel() + "]" ) ;
            leadLagLabel.setForeground( topicStats.getScoreColor() ) ;
            zoneLabelPanel.setScore( topicStats.getBurnStressScore() ) ;
            
            currentBurnLabel.setText( "[" + burnMeter.getCurrentBurnRate() + "]" ) ;
            requiredBurnLabel.setText( "[" + burnMeter.getRequiredBurnRate() + "]" ) ;
            
            if( burnMeter.getRequiredBurnRate() > burnMeter.getCurrentBurnRate() ) {
                requiredBurnLabel.setForeground( Color.RED ) ;
            }
            else {
                requiredBurnLabel.setForeground( Color.GREEN ) ;
            }
            
            pigeonPanel.setNumPigeons( topicStats.getNumPigeonedProblems() ) ;
            
            overshootLabel.setText( "{" + topicStats.getNumOvershootDays() + "}" ) ;
            if( burnMeter.getOvershootDays() > 0 ) {
                overshootLabel.setForeground( Color.RED ) ;
            }
            else {
                overshootLabel.setForeground( Color.GREEN ) ;
            }
        }
        else {
            pigeonPanel.setNumPigeons( 0 ) ;
            currentBurnLabel.setText( "" ) ;
            requiredBurnLabel.setText( "" ) ;
            overshootLabel.setText( "" ) ;
            leadLagLabel.setText( "" ) ;
            zoneLabelPanel.setScore( 0.0 ) ;
        }
    }

    private static Color zoneFractionColor( double fraction ) {
        Color green = Color.getHSBColor( 0.33f, 1.0f, 0.75f ) ;
        Color gray  = Color.GRAY ;
        Color red   = Color.getHSBColor( 0.00f, 1.0f, 0.75f ) ;
        return fraction <= 0.5
            ? interpolateColor( green, gray, (float)( fraction * 2.0 ) )
            : interpolateColor( gray,  red,  (float)( (fraction - 0.5) * 2.0 ) ) ;
    }

    private static Color interpolateColor( Color a, Color b, float t ) {
        return new Color(
            Math.round( a.getRed()   + t * (b.getRed()   - a.getRed()) ),
            Math.round( a.getGreen() + t * (b.getGreen() - a.getGreen()) ),
            Math.round( a.getBlue()  + t * (b.getBlue()  - a.getBlue()) )
        ) ;
    }

}
