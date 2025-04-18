package com.sandy.sconsole.core.ui.screen.tiles;

import com.sandy.sconsole.core.ui.screen.Tile;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static com.sandy.sconsole.core.ui.uiutil.SwingUtils.createEmptyLabel;

@Slf4j
public class ImageTile extends Tile {

    private JLabel imgLabel = null ;

    public ImageTile() {
        super( false ) ;
        setUpUI() ;
    }

    private void setUpUI() {
        imgLabel = createEmptyLabel( theme ) ;
        
        super.setLayout( new BorderLayout() ) ;
        super.add( imgLabel, BorderLayout.CENTER ) ;
    }

    public void setImage( BufferedImage image ) {
        imgLabel.setIcon( new ImageIcon( image ) ) ;
    }
}
