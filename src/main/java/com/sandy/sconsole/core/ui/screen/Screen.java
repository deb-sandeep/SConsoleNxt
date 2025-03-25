package com.sandy.sconsole.core.ui.screen;

import com.sandy.sconsole.core.ui.screen.util.AbstractPanel;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

import static com.sandy.sconsole.core.ui.screen.Tile.isTile;

@Slf4j
@Setter
@Getter
public abstract class Screen extends AbstractPanel {
    
    public enum LifecycleMethodType {
        INITIALIZE, BEFORE_ACTIVATION, BEFORE_DEACTIVATION
    }
    
    private static final int DEF_EPHEMERAL_LIFE_SPAN = 60 ; // In seconds

    private String  id ;
    private String  screenName;
    private int     priority = 0 ;
    private boolean ephemeral = false ;
    private int     ephemeralLifeSpan = -1 ;
    
    protected Screen( String id, String screenName ) {
        this( id, screenName, DEF_EPHEMERAL_LIFE_SPAN ) ;
    }
    
    protected Screen( String id, String screenName, int ephemeralLifeSpan ) {
        this.id = id ;
        this.screenName = screenName;
        this.withLifeSpan( ephemeralLifeSpan ) ;
    }
    
    public Screen withPriority( int priority ) {
        this.priority = priority ;
        return this ;
    }
    
    public Screen asPerpetual() {
        this.ephemeral = false ;
        this.ephemeralLifeSpan = -1 ;
        return this ;
    }
    
    public Screen withLifeSpan( int lifeSpan ) {
        this.ephemeral = true ;
        this.ephemeralLifeSpan = lifeSpan ;
        return this ;
    }
    
    public final void invokeLifecycleMethod( LifecycleMethodType methodType ) {
        
        try {
            // In case of initialization and activation, the screen gets initialized first
            // followed by all the tiles. For deactivation, the reverse order is followed.
            if( methodType == LifecycleMethodType.INITIALIZE ) {
                this.initialize() ;
            }
            else if( methodType == LifecycleMethodType.BEFORE_ACTIVATION ) {
                this.beforeActivation() ;
            }
            
            Field[] fields = this.getClass().getDeclaredFields() ;
            for( Field field : fields ) {
                if( isTile( field.getType() ) ) {
                    log.debug( "    Found a tile. {}", field.getName() ) ;
                    if( !field.canAccess( this ) ) {
                        field.setAccessible( true ) ;
                    }
                    
                    Tile tile = ( Tile )field.get( this ) ;
                    switch( methodType ) {
                        case INITIALIZE -> tile.initialize() ;
                        case BEFORE_ACTIVATION -> tile.beforeActivation() ;
                        case BEFORE_DEACTIVATION -> tile.beforeDeactivation() ;
                    }
                }
            }
            
            if( methodType == LifecycleMethodType.BEFORE_DEACTIVATION ) {
                this.beforeDeactivation() ;
            }
        }
        catch( IllegalAccessException e ) {
            log.error( "Error calling screen lifecycle method", e ) ;
            throw new RuntimeException( e );
        }
    }
    
    /**
     * This method needs to be called on the screen post creation to delegate
     * the screen initialization to this instance. This method should be
     * overridden by the concrete subclasses. It is a best practice to call
     * <code>super.initialize()</code> before the subclasses start their
     * own processing.
     */
    public abstract void initialize() ;

    protected void setUpBaseUI( UITheme theme ) {
        super.setBackground( UITheme.BG_COLOR ) ;
        super.setDefaultTableLayout() ;
    }
    
    /** This method is called before a screen is made visible. */
    public void beforeActivation() {}

    /** This method is called before a screen is removed from display. */
    public void beforeDeactivation() {}
}
