package com.sandy.sconsole.core.remote;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

public class KeySet {

    @Data
    private static class KeyInfo {
        private boolean enabled ;
        private String displayText ;

        KeyInfo( String displayText, boolean enabled ) {
            this.displayText = displayText ;
            this.enabled = enabled ;
        }
    }

    public static final KeySet DISABLED_KEY_SET = new KeySet( false ) ;

    private final Map<RemoteKey, KeyInfo> keyInfo = new HashMap<>() ;

    public KeySet() {
        this( true ) ;
    }

    public KeySet( boolean allEnabled ) {
        // By default, all keys are enabled.
        for( RemoteKey key : RemoteKey.values() ) {
            KeyInfo info = new KeyInfo( key.getDisplayText(), allEnabled ) ;
            keyInfo.put( key, info ) ;
        }
    }

    private KeySet setEnabled( RemoteKey.KeyType type, boolean enable ) {
        for( RemoteKey key : keyInfo.keySet() ) {
            if( key.getType() == type ) {
                keyInfo.get( key ).enabled = enable ;
            }
        }
        return this ;
    }

    public KeySet disableFunctionKeys() {
        return setEnabled( RemoteKey.KeyType.FUNCTION, false ) ;
    }

    public KeySet disableNumericKeys() {
        return setEnabled( RemoteKey.KeyType.NUMERIC, false ) ;
    }

    public KeySet disableControlKeys() {
        return setEnabled( RemoteKey.KeyType.CONTROL, false ) ;
    }

    public KeySet disableMovementKeys() {
        return setEnabled( RemoteKey.KeyType.MOVEMENT, false ) ;
    }

    public KeySet disableAllKeys() {
        for( RemoteKey key : keyInfo.keySet() ) {
            keyInfo.get( key ).enabled = false ;
        }
        return this ;
    }

    public KeySet disableKeys( RemoteKey... keys ) {
        for( RemoteKey key : keys ) {
            keyInfo.get( key ).enabled = false ;
        }
        return this ;
    }

    public KeySet enableFunctionKeys() {
        return setEnabled( RemoteKey.KeyType.FUNCTION, true ) ;
    }

    public KeySet enableNumericKeys() {
        return setEnabled( RemoteKey.KeyType.NUMERIC, true ) ;
    }

    public KeySet enableControlKeys() {
        return setEnabled( RemoteKey.KeyType.CONTROL, true ) ;
    }

    public KeySet enableMovementKeys() {
        return setEnabled( RemoteKey.KeyType.MOVEMENT, true ) ;
    }

    public KeySet enableAllKeys() {
        for( RemoteKey key : keyInfo.keySet() ) {
            keyInfo.get( key ).enabled = true ;
        }
        return this ;
    }

    public KeySet enableKeys( RemoteKey... keys ) {
        for( RemoteKey key : keys ) {
            keyInfo.get( key ).enabled = true ;
        }
        return this ;
    }

    public KeySet setDisplayText( RemoteKey key, String display ) {
        keyInfo.get( key ).displayText = display ;
        return this ;
    }

    public boolean isKeyEnabled( RemoteKey key ) {
        return keyInfo.get( key ).enabled ;
    }
}
