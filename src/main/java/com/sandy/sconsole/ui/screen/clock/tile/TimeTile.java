package com.sandy.sconsole.ui.screen.clock.tile;

import com.sandy.sconsole.core.ui.screen.tiles.StringTile;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeTile extends StringTile {

    private static final SimpleDateFormat SDF = new SimpleDateFormat( "HH:mm:ss" ) ;

    public TimeTile( int fontSize ) {
        super( fontSize ) ;
    }

    public void updateDisplay( Calendar calendar ) {
        setLabelText( SDF.format( calendar.getTime() ) ) ;
    }
}
