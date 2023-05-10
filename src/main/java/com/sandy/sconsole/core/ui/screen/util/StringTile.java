package com.sandy.sconsole.core.ui.screen.util;

import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;

public class StringTile extends Tile {

    @Getter @Setter private String content ;

    private JLabel textLabel = null ;

    public StringTile( UITheme theme, int fontSize ) {
        this( theme, Font.PLAIN, fontSize, JLabel.CENTER, JLabel.CENTER, false ) ;
    }

    public StringTile( UITheme theme, int fontSize, int hAlign ) {
        this( theme, Font.PLAIN, fontSize, hAlign, JLabel.CENTER, false ) ;
    }

    public StringTile( UITheme theme, int fontSize, int hAlign, int vAlign ) {
        this( theme, Font.PLAIN, fontSize, hAlign, vAlign, false ) ;
    }

    public StringTile( UITheme theme, int fontStyle, int fontSize, int hAlign, int vAlign, boolean isBordered ) {
        super( theme, isBordered ) ;
        setUpUI( theme, fontStyle, fontSize, hAlign, vAlign ) ;
    }

    private void setUpUI( UITheme theme, int fontStyle, int fontSize, int hAlign, int vAlign ) {
        textLabel = super.getTemplateLabel() ;
        textLabel.setFont( theme.getLabelFont( fontStyle, fontSize ) ) ;
        textLabel.setHorizontalAlignment( hAlign ) ;
        textLabel.setVerticalAlignment( vAlign ) ;
        super.add( textLabel, BorderLayout.CENTER ) ;
    }

    // Assumed that this method is invoked in Swing event thread.
    public void setLabelText( String text ) {
        this.textLabel.setText( text == null ? "" : text ) ;
    }

    public void setHorizontalAlignment( int hAlign ) {
        this.textLabel.setHorizontalAlignment( hAlign ) ;
    }

    // Assumed that this method is invoked in Swing event thread.
    public void setLabelFont( Font font ) {
        this.textLabel.setFont( font ) ;
    }

    // Assumed that this method is invoked in Swing event thread.
    public void setLabelForeground( Color color ) {
        this.textLabel.setForeground( color ) ;
    }

    // Assumed that this method is invoked in Swing event thread.
    public void setLabelHTMLText( String text ) {
        this.textLabel.setText( "<html>" + text + "</html>" ) ;
    }
}
