package com.sandy.sconsole.core.ui.screen.util;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.ui.screen.Screen;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScreenBuilder  {

    private final SConsole app ;
    private final UITheme theme ;

    private Class<? extends Screen> screenCls ;

    private String name ;
    private String icon ;

    private ScreenBuilder( SConsole app ) {
        this.app = app ;
        this.theme = app.getTheme() ;
    }

    public static ScreenBuilder instance( SConsole app ) {
        return new ScreenBuilder( app ) ;
    }

    public ScreenBuilder withName( String name ) {
        this.name = name ;
        return this ;
    }

    public ScreenBuilder withScreenClass( Class<? extends Screen> cls ) {
        this.screenCls = cls ;
        return this ;
    }

    public ScreenBuilder withIcon( String icon ) {
        this.icon = icon ;
        return this ;
    }

    public Screen build() {

        Screen screen = app.getCtx().getBean( this.screenCls ) ;

        screen.setName( name ) ;
        screen.setIcon( icon == null ? theme.getDefaultScreenIconName() : icon ) ;

        screen.initialize( theme ) ;

        return screen ;
    }
}
