package com.sandy.sconsole.core.ui.screen;

import com.sandy.sconsole.core.ui.uiutil.UITheme;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScreenBuilder  {

    private final UITheme theme ;

    private Class<? extends Screen> screenCls ;
    private String name ;
    private Screen parentScreen ;
    private String icon ;
    private boolean showOnStartup = false ;

    private ScreenBuilder( UITheme theme ) {
        this.theme = theme ;
    }

    public static ScreenBuilder instance( UITheme theme ) {
        return new ScreenBuilder( theme ) ;
    }

    public ScreenBuilder withName( String name ) {
        this.name = name ;
        return this ;
    }

    public ScreenBuilder withScreenClass( Class<? extends Screen> cls ) {
        this.screenCls = cls ;
        return this ;
    }

    public ScreenBuilder withParentScreen( Screen screen ) {
        this.parentScreen = screen ;
        return this ;
    }

    public ScreenBuilder withIcon( String icon ) {
        this.icon = icon ;
        return this ;
    }

    public ScreenBuilder withShowOnStartup() {
        this.showOnStartup = true ;
        return this ;
    }

    public Screen build() throws Exception {
        Screen screen = screenCls.getConstructor().newInstance();

        screen.setName( name ) ;
        screen.setParentScreen( parentScreen ) ;
        screen.setIcon( icon == null ? theme.getDefaultScreenIconName() : icon ) ;
        screen.setShowOnStartup( showOnStartup ) ;

        screen.initialize( theme ) ;

        return screen ;
    }
}
