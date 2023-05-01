package com.sandy.sconsole.screen.clock.tile;

import com.sandy.sconsole.core.ui.Screen;
import com.sandy.sconsole.core.ui.uiutil.StringTile;
import com.sandy.sconsole.core.ui.uiutil.UITheme;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeTile extends StringTile {

    private static final SimpleDateFormat SDF = new SimpleDateFormat( "HH:mm:ss" ) ;

    public TimeTile( Screen parent, UITheme theme, int fontSize ) {
        super( parent, theme, fontSize ) ;
    }

    public void updateDisplay( Calendar calendar ) {
        setLabelText( SDF.format( calendar.getTime() ) ) ;
    }
}
