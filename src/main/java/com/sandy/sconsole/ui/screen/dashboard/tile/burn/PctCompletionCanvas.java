package com.sandy.sconsole.ui.screen.dashboard.tile.burn;

import com.sandy.sconsole.core.ui.uiutil.SwingUtils;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.state.ActiveTopicStatistics;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
@Scope( "prototype" )
class PctCompletionCanvas extends JPanel {
    
    private final Insets BORDER = new Insets( 2, 10, 2, 10 ) ;
    
    @Autowired private UITheme theme ;
    
    @Setter private ActiveTopicStatistics topicStats ;
    
    public PctCompletionCanvas() {
        setPreferredSize( new Dimension( 10, 10 ) ) ;
    }
    
    @PostConstruct
    public void init() {
        setBackground( UITheme.BG_COLOR ) ;
    }
    
    void refreshUI() {
        super.repaint() ;
    }
    
    @Override
    public void paint( Graphics g ) {
        super.paint( g ) ;
        
        if( topicStats != null ) {
            int width = getWidth() - BORDER.left - BORDER.right ;
            int height = getHeight() - BORDER.top - BORDER.bottom ;
            
            float pixelsPerQuestion = ((float)width)/topicStats.getTotalProblemsCount() ;
            int partition = (int)(pixelsPerQuestion * topicStats.getCompletedProblemsCount() ) ;
            
            g.setColor( Color.GREEN.darker() .darker()) ;
            g.fillRect( BORDER.left, BORDER.top, partition, height ) ;
            
            g.setColor( SwingUtils.darkerColor( Color.RED, 0.3F ) ) ;
            g.fillRect( BORDER.left + partition + 1, BORDER.top, width - partition, height ) ;
        }
    }
}
