package com.sandy.sconsole.ui.screen.clock.tile;

import com.sandy.sconsole.core.ui.screen.tiles.StringTile;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateTile extends StringTile {

    private final SimpleDateFormat dateFormat ;

    public DateTile( int fontSize ) {
        this( "dd MMM, EEEE", fontSize ) ;
    }

    public DateTile( String dateFormat, int fontSize ) {
        super( fontSize ) ;
        this.dateFormat = new SimpleDateFormat( dateFormat ) ;
    }

    public void updateDisplay( Calendar calendar ) {
        setLabelText( dateFormat.format( calendar.getTime() ) ) ;
    }
}
