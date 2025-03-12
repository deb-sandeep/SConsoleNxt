package com.sandy.sconsole.core.ui.screen;

import lombok.Data;

@Data
public class ScreenCmd {
    
    public enum CmdType {
        REFRESH_CONFIG,
        CHANGE_SCREEN,
    }
    
    public static ScreenCmd REFRESH_CONFIG_CMD = new ScreenCmd( CmdType.REFRESH_CONFIG ) ;
    public static ScreenCmd CHANGE_SCREEN_CMD( String newScreenName ) {
        return new ScreenCmd( CmdType.CHANGE_SCREEN, newScreenName ) ;
    }
    
    private CmdType type ;
    private String newScreenName ;
    
    public ScreenCmd() {}
    
    private ScreenCmd( CmdType type ) {
        this.type = type ;
    }
    
    private ScreenCmd( CmdType type, String newScreenName ) {
        this.type = CmdType.CHANGE_SCREEN ;
        this.newScreenName = newScreenName ;
    }
}
