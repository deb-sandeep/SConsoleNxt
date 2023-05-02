package com.sandy.sconsole.core.remote;

public enum RemoteKey {

    FN_A      ( KeyType.FUNCTION, "Fn A" ),
    FN_B      ( KeyType.FUNCTION, "Fn B" ),
    FN_C      ( KeyType.FUNCTION, "Fn C" ),
    FN_D      ( KeyType.FUNCTION, "Fn D" ),
    FN_E      ( KeyType.FUNCTION, "Fn E" ),

    VK_1      ( KeyType.NUMERIC, '1', "1" ),
    VK_2      ( KeyType.NUMERIC, '2', "2" ),
    VK_3      ( KeyType.NUMERIC, '3', "3" ),
    VK_4      ( KeyType.NUMERIC, '4', "4" ),
    VK_5      ( KeyType.NUMERIC, '5', "5" ),
    VK_6      ( KeyType.NUMERIC, '6', "6" ),
    VK_7      ( KeyType.NUMERIC, '7', "7" ),
    VK_8      ( KeyType.NUMERIC, '8', "8" ),
    VK_9      ( KeyType.NUMERIC, '9', "9" ),
    VK_0      ( KeyType.NUMERIC, '0', "0" ),
    VK_DOT    ( KeyType.NUMERIC, '.', "." ),
    VI_MINUS  ( KeyType.NUMERIC, '-', "-" ),

    CT_OK     ( KeyType.CONTROL, "Ok" ),
    CT_CANCEL ( KeyType.CONTROL, "Cancel" ),
    CT_PAUSE  ( KeyType.CONTROL, "Pause" ),
    CT_HOME   ( KeyType.CONTROL, "Home" ),

    MV_UP     ( KeyType.MOVEMENT, "Up" ),
    MV_DOWN   ( KeyType.MOVEMENT, "Down" ),
    MV_RIGHT  ( KeyType.MOVEMENT, "Right" ),
    MV_LEFT   ( KeyType.MOVEMENT, "Left" );

    public enum KeyType {
        FUNCTION,
        NUMERIC,
        CONTROL,
        MOVEMENT
    }

    private final KeyType keyType ;
    private final char keyChar ;
    private final String displayText ;

    RemoteKey( KeyType type ) {
        this( type, ( char )-1, "" ) ;
    }

    RemoteKey( KeyType type, String displayText ) {
        this( type, (char)-1, displayText ) ;
    }

    RemoteKey( KeyType type, char keyChar, String displayText ) {
        this.keyType = type ;
        this.keyChar = keyChar ;
        this.displayText = displayText ;
    }

    public KeyType getType() { return this.keyType ; }

    public boolean isFunctionKey() {
        return this.keyType == KeyType.FUNCTION ;
    }

    public boolean isNumericKey() {
        return this.keyType == KeyType.NUMERIC ;
    }

    public boolean isControlKey() {
        return this.keyType == KeyType.CONTROL ;
    }

    public boolean isMovementKey() {
        return this.keyType == KeyType.MOVEMENT ;
    }

    public char getKeyChar() {
        return this.keyChar ;
    }

    public String getDisplayText() {
        return displayText.equals( "" ) ? toString() : displayText ;
    }
}
