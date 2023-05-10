package com.sandy.sconsole.screen.clock.tile;

import com.sandy.sconsole.core.ui.screen.util.StringTile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateTile extends StringTile {

    private final SimpleDateFormat dateFormat ;

    public DateTile( UITheme theme, int fontSize ) {
        this( "dd MMM, EEEE", theme, fontSize ) ;
    }

    public DateTile( String dateFormat, UITheme theme, int fontSize ) {
        super( theme, fontSize ) ;
        this.dateFormat = new SimpleDateFormat( dateFormat ) ;
    }

    public void updateDisplay( Calendar calendar ) {
        setLabelText( dateFormat.format( calendar.getTime() ) ) ;
    }
}
