package com.sandy.sconsole.ui.screen.session.tile.center;

import com.sandy.sconsole.core.ui.screen.Tile;
import com.sandy.sconsole.ui.screen.session.tile.center.face.CoachingTileFace;
import com.sandy.sconsole.ui.screen.session.tile.center.face.ExerciseTileFace;
import com.sandy.sconsole.ui.screen.session.tile.center.face.TheoryTileFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
@Scope( "prototype" )
public class SessionCenterTile extends Tile {

    private static final String COACHING_FACE = "COACHING_FACE" ;
    private static final String THEORY_FACE   = "THEORY_FACE" ;
    private static final String EXERCISE_FACE = "EXERCISE_FACE" ;

    @Autowired private CoachingTileFace coachingTileFace ;
    @Autowired private TheoryTileFace   theoryTileFace ;
    @Autowired private ExerciseTileFace exerciseTileFace;

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

        add( coachingTileFace, COACHING_FACE ) ;
        add( theoryTileFace, THEORY_FACE ) ;
        add( exerciseTileFace, EXERCISE_FACE ) ;

        coachingTileFace.initialize() ;
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
        if( sessionType == null ) {
            return THEORY_FACE ;
        }

        return switch( sessionType ) {
            case "Coaching" -> COACHING_FACE ;
            case "Theory" -> THEORY_FACE ;
            case "Exercise" -> EXERCISE_FACE;
            default -> THEORY_FACE ;
        } ;
    }

    private Tile getFace( String faceName ) {
        return switch( faceName ) {
            case COACHING_FACE -> coachingTileFace ;
            case THEORY_FACE -> theoryTileFace ;
            case EXERCISE_FACE -> exerciseTileFace;
            default -> theoryTileFace ;
        } ;
    }
}
