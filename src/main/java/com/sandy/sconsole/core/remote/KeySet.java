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

    private Map<RemoteKey, KeyInfo> keySet = new HashMap<>() ;

    public KeySet() {
        this( true ) ;
    }

    public KeySet( boolean allEnabled ) {
        // By default all keys are enabled.
        for( RemoteKey key : RemoteKey.values() ) {
            KeyInfo info = new KeyInfo( key.getDisplayText(), allEnabled ) ;
            keySet.put( key, info ) ;

        }
    }

    private void setEnabled( RemoteKey.KeyType type, boolean enable ) {
        for( RemoteKey key : keySet.keySet() ) {
            if( key.getType() == type ) {
                keySet.get( key ).enabled = enable ;
            }
        }
    }

    public void disableFunctionKeys() {
        setEnabled( RemoteKey.KeyType.FUNCTION, false ) ;
    }

    public void disableNumericKeys() {
        setEnabled( RemoteKey.KeyType.NUMERIC, false ) ;
    }

    public void disableControlKeys() {
        setEnabled( RemoteKey.KeyType.CONTROL, false ) ;
    }

    public void disableMovementKeys() {
        setEnabled( RemoteKey.KeyType.MOVEMENT, false ) ;
    }

    public void disableAllKeys() {
        for( RemoteKey key : keySet.keySet() ) {
            keySet.get( key ).enabled = false ;
        }
    }

    public void disableKeys( RemoteKey... keys ) {
        for( RemoteKey key : keys ) {
            keySet.get( key ).enabled = false ;
        }
    }

    public void enableFunctionKeys() {
        setEnabled( RemoteKey.KeyType.FUNCTION, true ) ;
    }

    public void enableNumericKeys() {
        setEnabled( RemoteKey.KeyType.NUMERIC, true ) ;
    }

    public void enableControlKeys() {
        setEnabled( RemoteKey.KeyType.CONTROL, true ) ;
    }

    public void enableMovementKeys() {
        setEnabled( RemoteKey.KeyType.MOVEMENT, true ) ;
    }

    public void enableAllKeys() {
        for( RemoteKey key : keySet.keySet() ) {
            keySet.get( key ).enabled = true ;
        }
    }

    public void enableKeys( RemoteKey... keys ) {
        for( RemoteKey key : keys ) {
            keySet.get( key ).enabled = true ;
        }
    }

    public void setDisplayText( RemoteKey key, String display ) {
        keySet.get( key ).displayText = display ;
    }

    public boolean isKeyEnabled( RemoteKey key ) {
        return keySet.get( key ).enabled ;
    }
}
