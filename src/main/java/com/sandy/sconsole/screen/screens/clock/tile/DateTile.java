package com.sandy.sconsole.screen.screens.clock.tile;

import com.sandy.sconsole.core.ui.screen.util.StringTile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateTile extends StringTile {

    private static final SimpleDateFormat SDF = new SimpleDateFormat( "dd MMMM, EEEE" ) ;

    public DateTile( UITheme theme, int fontSize ) {
        super( theme, fontSize ) ;
    }

    public void updateDisplay( Calendar calendar ) {
        setLabelText( SDF.format( calendar.getTime() ) ) ;
    }
}
