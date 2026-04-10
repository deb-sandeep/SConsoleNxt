package com.sandy.sconsole.ui.screen.session.tile.center;

import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.ui.screen.session.tile.center.face.BlankTileFace;
import com.sandy.sconsole.ui.screen.session.tile.center.face.ExerciseTileFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
@Scope( "prototype" )
public class SessionCenterTile extends Tile {

    private static final String THEORY_FACE   = "THEORY_FACE" ;
    private static final String EXERCISE_FACE = "EXERCISE_FACE" ;

    @Autowired private ExerciseTileFace exerciseTileFace;
    @Autowired private BlankTileFace    theoryTileFace  ;

    private final CardLayout cardLayout = new CardLayout() ;

    private Tile activeFace = null ;
    private boolean initialized = false ;
    private boolean activated = false ;

    @Override
    public void initialize() {

        if( initialized ) {
            return ;
        }

        removeAll() ;
        setLayout( cardLayout ) ;

        add( theoryTileFace, THEORY_FACE ) ;
        add( exerciseTileFace, EXERCISE_FACE ) ;

        theoryTileFace.initialize() ;
        exerciseTileFace.initialize() ;

        activeFace = theoryTileFace ;
        cardLayout.show( this, THEORY_FACE ) ;
        initialized = true ;
    }

    @Override
    public void beforeActivation() {
        activated = true ;
        if( activeFace != null ) {
            activeFace.beforeActivation() ;
        }
    }

    @Override
    public void beforeDeactivation() {
        if( activeFace != null ) {
            activeFace.beforeDeactivation() ;
        }
        activated = false ;
    }

    public void activateFace( String sessionType ) {

        String faceName = getFaceName( sessionType ) ;
        Tile face = getFace( faceName ) ;

        if( face == activeFace ) {
            return ;
        }

        if( activated && activeFace != null ) {
            activeFace.beforeDeactivation() ;
        }

        activeFace = face ;
        cardLayout.show( this, faceName ) ;
        revalidate() ;
        repaint() ;

        if( activated && activeFace != null ) {
            activeFace.beforeActivation() ;
        }
    }

    private String getFaceName( String sessionType ) {
        
        if( "Coaching".equals( sessionType ) ||
            "Exercise".equals( sessionType ) ) {
            return EXERCISE_FACE ;
        }
        return THEORY_FACE ;
    }

    private Tile getFace( String faceName ) {
        if( EXERCISE_FACE.equals( faceName ) ) {
            return exerciseTileFace ;
        }
        return theoryTileFace ;
    }
}
