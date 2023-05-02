package com.sandy.sconsole.core.ui.screen.util;

import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;

public class StringTile extends Tile {

    @Getter @Setter private String content ;

    private JLabel textLabel = null ;

    public StringTile( Screen parent, UITheme theme, int fontSize ) {
        this( parent, theme, Font.PLAIN, fontSize, JLabel.CENTER, JLabel.CENTER, false ) ;
    }

    public StringTile( Screen parent, UITheme theme, int fontSize, int hAlign ) {
        this( parent, theme, Font.PLAIN, fontSize, hAlign, JLabel.CENTER, false ) ;
    }

    public StringTile( Screen parent, UITheme theme, int fontSize, int hAlign, int vAlign ) {
        this( parent, theme, Font.PLAIN, fontSize, hAlign, vAlign, false ) ;
    }

    public StringTile( Screen parent, UITheme theme, int fontStyle, int fontSize, int hAlign, int vAlign, boolean isBordered ) {
        super( parent, theme, isBordered ) ;
        setUpUI( theme, fontStyle, fontSize, hAlign, vAlign ) ;
    }

    private void setUpUI( UITheme theme, int fontStyle, int fontSize, int hAlign, int vAlign ) {
        textLabel = super.getTemplateLabel() ;
        textLabel.setFont( theme.getLabelFont( fontStyle, fontSize ) ) ;
        textLabel.setHorizontalAlignment( hAlign ) ;
        textLabel.setVerticalAlignment( vAlign ) ;
        super.add( textLabel, BorderLayout.CENTER ) ;
    }

    public void setLabelText( String text ) {
        SwingUtilities.invokeLater( () -> this.textLabel.setText( text ) ) ;
    }

    public void setLabelFont( Font font ) {
        SwingUtilities.invokeLater( () -> this.textLabel.setFont( font ) ) ;
    }

    public void setLabelForeground( Color color ) {
        SwingUtilities.invokeLater( () -> this.textLabel.setForeground( color ) ) ;
    }

    public void setLabelHTMLText( String text ) {
        SwingUtilities.invokeLater( () -> this.textLabel.setText( "<html>" + text + "</html>" ) ) ;
    }
}
