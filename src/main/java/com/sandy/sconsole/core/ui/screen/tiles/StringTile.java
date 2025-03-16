package com.sandy.sconsole.core.ui.screen.tiles;

import com.sandy.sconsole.core.ui.screen.Tile;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;

import static com.sandy.sconsole.core.ui.uiutil.SwingUtils.createEmptyLabel;

public class StringTile extends Tile {

    @Getter @Setter private String content ;

    private JLabel textLabel = null ;
    
    public StringTile() {
        this( 20 ) ;
    }

    public StringTile( int fontSize ) {
        this( Font.PLAIN, fontSize, JLabel.CENTER, JLabel.CENTER, false ) ;
    }

    public StringTile( int fontSize, int hAlign ) {
        this( Font.PLAIN, fontSize, hAlign, JLabel.CENTER, false ) ;
    }

    public StringTile( int fontSize, int hAlign, int vAlign ) {
        this( Font.PLAIN, fontSize, hAlign, vAlign, false ) ;
    }

    public StringTile( int fontStyle, int fontSize, int hAlign, int vAlign, boolean isBordered ) {
        super( isBordered ) ;
        setUpUI( fontStyle, fontSize, hAlign, vAlign ) ;
    }

    private void setUpUI( int fontStyle, int fontSize, int hAlign, int vAlign ) {
        textLabel = createEmptyLabel( theme ) ;
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
