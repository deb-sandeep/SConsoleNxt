package com.sandy.sconsole.core.ui.screen;

import com.sandy.sconsole.core.ui.uiutil.UITheme;

import javax.swing.*;
import java.awt.*;

public abstract class Tile extends JPanel {

    protected Screen parent ;
    protected UITheme theme ;

    protected Tile( Screen parent, UITheme theme, boolean isBordered ) {
        this.parent = parent ;
        this.theme = theme ;
        setUpUI( theme, isBordered ) ;
    }

    private void setUpUI( UITheme theme, boolean isBordered ) {
        super.setBackground( theme.getBackgroundColor() ) ;
        super.setLayout( new BorderLayout() ) ;
        if( isBordered ) {
            super.setBorder( theme.getTileBorder() ) ;
        }
    }

    protected JLabel getTemplateLabel() {
        JLabel label = new JLabel() ;
        label.setHorizontalAlignment( SwingConstants.CENTER ) ;
        label.setVerticalAlignment( SwingConstants.CENTER ) ;
        label.setBackground( theme.getBackgroundColor() ) ;
        label.setForeground( theme.getTileForeground() ) ;
        label.setOpaque( true ) ;
        return label ;
    }
}
