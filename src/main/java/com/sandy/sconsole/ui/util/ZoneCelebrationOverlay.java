package com.sandy.sconsole.ui.util;

import com.sandy.sconsole.core.ui.SConsoleFrame;
import com.sandy.sconsole.core.ui.uiutil.UITheme;
import com.sandy.sconsole.state.ActiveTopicStatistics;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Full-screen celebration shown when the active session's topic crosses
 * into a better burn-stress zone (AHEAD or higher - see
 * TopicBurnChartTile.checkZoneImprovement() for the trigger). Deliberately
 * blocks input for its short duration - a real pause, not a side effect.
 *
 * Uses a cheap particle-burst simulation rather than an animated GIF/sprite:
 * on the Raspberry Pi this runs on, a single full-screen bitmap frame
 * already costs ~30-40MB (see SConsoleFrame.doCapture()'s comment on why
 * that buffer is reused rather than reallocated), so decoding/holding
 * multiple such frames for a GIF risked real memory/GC pressure. Particles
 * are just a handful of floats each, drawn with plain Graphics2D.fillOval
 * calls - negligible memory, cheap per-frame cost.
 */
@Slf4j
@Component
public class ZoneCelebrationOverlay extends JPanel {

    // ---- Tweakable constants -----------------------------------------
    // Animation loop tick - lower = smoother but more CPU per second.
    private static final int TICK_MS = 100 ;
    
    // How long the background dim takes to darken to MAX_DIM_ALPHA.
    private static final int FADE_IN_MS = 1000 ;
    
    // How long the overlay stays fully dim - this is the window bursts are
    // spread across (see startCelebration()'s pendingBurstTimes setup), so
    // raising this spreads the same burst count out more slowly.
    private static final int HOLD_MS = 5000 ;
    
    // How long the background dim takes to fade back to transparent.
    private static final int FADE_OUT_MS = 1000 ;
    
    // Total celebration duration - derived, not directly tweakable.
    private static final int TOTAL_MS = FADE_IN_MS + HOLD_MS + FADE_OUT_MS ;
    
    // Peak darkness of the background dim layer, 0 (no dim) - 255 (opaque
    // black). Also caps the message text's fade-in/out alpha (see
    // paintComponent()'s textAlpha calc).
    private static final int MAX_DIM_ALPHA = 220 ;
    
    // Downward acceleration applied to every particle each tick - higher
    // makes bursts fall/collapse faster, lower makes them float longer.
    private static final double GRAVITY = 0.3 ;

    // Per-burst visuals - every burst at every zone level looks like this
    // now (this used to scale down for lower tiers, which made AHEAD look
    // bland; only burstCount still scales with tier, see
    // startCelebration()).
    private static final float BURST_RADIUS = 190f ; // how far particles spread from the burst center
    private static final float PARTICLE_SIZE = 6.6f ; // radius (px) of each particle dot
    private static final int   PARTICLES_PER_BURST = 74 ;   // particles spawned per burst
    private static final Color[] BURST_PALETTE = {
        Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.CYAN, Color.MAGENTA
    } ; // pool a burst's particles pick a color from at random - add/remove/swap freely

    private static final Random RANDOM = new Random() ;

    // Size/weight of the congratulatory zone-name text (color comes from
    // ActiveTopicStatistics.scoreColor(), not from here).
    private static final Font MESSAGE_FONT = UITheme.BASE_FONT.deriveFont( Font.BOLD, 72f ) ;

    @Autowired private SConsoleFrame sConsoleFrame ;

    private final List<Particle> particles = new ArrayList<>() ;
    private final List<Integer>  pendingBurstTimes  = new ArrayList<>() ;

    private Timer   timer         = null ;
    private int     elapsedMs     = 0 ;
    private int     dimAlpha      = 0 ;
    private boolean firstBurstDone = false ;
    private String  message       = null ;
    private Color   messageColor  = Color.WHITE ;

    private static class Particle {
        double x, y, vx, vy ;
        float  radius ;
        int    life, maxLife ;
        Color  color ;
    }

    @PostConstruct
    public void init() {
        setOpaque( false ) ;
        setVisible( false ) ;
        sConsoleFrame.setGlassPane( this ) ;
    }

    /**
     * Kicks off the celebration for the given ZONE_LABELS/ZONE_BOUNDS index
     * (0 = UNSTOPPABLE!! ... 6 = AHEAD). Safe to call from any thread - the
     * actual animation always runs on the EDT.
     */
    public void celebrate( int zoneIndex ) {
        SwingUtilities.invokeLater( () -> startCelebration( zoneIndex ) ) ;
    }

    private void startCelebration( int zoneIndex ) {

        if( timer != null && timer.isRunning() ) {
            return ; // already celebrating - ignore an overlapping request
        }

        this.message = ActiveTopicStatistics.ZONE_LABELS[ zoneIndex ] ;

        double lo = ActiveTopicStatistics.ZONE_BOUNDS[ zoneIndex ] ;
        double hi = ActiveTopicStatistics.ZONE_BOUNDS[ zoneIndex + 1 ] ;
        this.messageColor = ActiveTopicStatistics.scoreColor( ( lo + hi ) / 2.0 ) ;

        particles.clear() ;
        pendingBurstTimes.clear() ;
        elapsedMs = 0 ;
        dimAlpha = 0 ;
        firstBurstDone = false ;

        // 1 burst for AHEAD(6), 3 for SLAYIN!(5), 5 for ROCKIN!!(4), ...
        // up to 13 for UNSTOPPABLE!!(0) - bursts are spread evenly across
        // the hold phase rather than all firing at once. Change the "2" to
        // widen/narrow the gap between tiers, or the "1" to raise/lower the
        // baseline burst count for AHEAD.
        int burstCount = 5 + 2 * ( 6 - zoneIndex ) ;
        for( int i = 0; i < burstCount; i++ ) {
            pendingBurstTimes.add( FADE_IN_MS + ( HOLD_MS * i / burstCount ) ) ;
        }

        setVisible( true ) ;

        timer = new Timer( TICK_MS, e -> tick() ) ;
        timer.start() ;
    }

    private void tick() {

        elapsedMs += TICK_MS ;
        dimAlpha = computeDimAlpha() ;

        pendingBurstTimes.removeIf( spawnAt -> {
            if( elapsedMs >= spawnAt ) {
                spawnBurst() ;
                return true ;
            }
            return false ;
        } ) ;

        updateParticles() ;
        repaint() ;

        if( elapsedMs >= TOTAL_MS ) {
            timer.stop() ;
            particles.clear() ;
            setVisible( false ) ;
        }
    }

    private int computeDimAlpha() {
        if( elapsedMs < FADE_IN_MS ) {
            return (int)( MAX_DIM_ALPHA * ( elapsedMs / (double)FADE_IN_MS ) ) ;
        }
        if( elapsedMs < FADE_IN_MS + HOLD_MS ) {
            return MAX_DIM_ALPHA ;
        }
        int fadeOutElapsed = elapsedMs - FADE_IN_MS - HOLD_MS ;
        return Math.max( 0, (int)( MAX_DIM_ALPHA * ( 1 - fadeOutElapsed / (double)FADE_OUT_MS ) ) ) ;
    }

    private void spawnBurst() {

        int width  = getWidth()  > 0 ? getWidth()  : 1920 ;
        int height = getHeight() > 0 ? getHeight() : 1080 ;

        int cx, cy ;
        if( !firstBurstDone ) {
            // The very first burst of every celebration is centered -
            // matches the "1 central burst for AHEAD" spec.
            cx = width / 2 ;
            cy = height / 2 ;
            firstBurstDone = true ;
        }
        else {
            // Non-first bursts spawn at a random point within the middle
            // 60% of the width and the 6%-70% vertical band (keeps them
            // off the very top/bottom edges). Widen/narrow these fractions
            // to spread bursts closer to or further from screen edges.
            cx = width  / 5 + RANDOM.nextInt( 3 * width  / 5 ) ;
            cy = height / 6 + RANDOM.nextInt( 3 * height / 5 ) ;
        }

        for( int i = 0; i < PARTICLES_PER_BURST; i++ ) {
            double angle = RANDOM.nextDouble() * 2 * Math.PI ;
            // Particle speed away from the burst center - scales with
            // BURST_RADIUS above; the /30.0 and the 0.5-1.5x random spread
            // are both tweakable to make bursts feel faster/tighter or
            // slower/looser.
            double speed = BURST_RADIUS / 25.0 * ( 0.5 + RANDOM.nextDouble() ) ;

            Particle p = new Particle() ;
            p.x = cx ;
            p.y = cy ;
            p.vx = Math.cos( angle ) * speed ;
            p.vy = Math.sin( angle ) * speed ;
            p.color = BURST_PALETTE[ RANDOM.nextInt( BURST_PALETTE.length ) ] ;
            p.radius = PARTICLE_SIZE ;
            // How many ticks (at TICK_MS each) a particle lives before
            // fading out completely - 35-50 ticks here is ~1.0-1.5s at the
            // default 30ms tick.
            p.maxLife = 35 + RANDOM.nextInt( 15 ) ;
            p.life = p.maxLife ;
            particles.add( p ) ;
        }
    }

    private void updateParticles() {
        for( Particle p : particles ) {
            p.x += p.vx ;
            p.y += p.vy ;
            p.vy += GRAVITY ;
            p.life-- ;
        }
        particles.removeIf( p -> p.life <= 0 ) ;
    }

    @Override
    protected void paintComponent( Graphics g ) {
        super.paintComponent( g ) ;
        Graphics2D g2 = (Graphics2D)g ;

        g2.setColor( new Color( 0, 0, 0, dimAlpha ) ) ;
        g2.fillRect( 0, 0, getWidth(), getHeight() ) ;

        for( Particle p : particles ) {
            int alpha = Math.max( 0, Math.min( 255, (int)( 255.0 * p.life / p.maxLife ) ) ) ;
            Color c = p.color ;
            g2.setColor( new Color( c.getRed(), c.getGreen(), c.getBlue(), alpha ) ) ;
            int d = (int)( 2 * p.radius ) ;
            g2.fillOval( (int)( p.x - p.radius ), (int)( p.y - p.radius ), d, d ) ;
        }

        if( message != null ) {
            int textAlpha = Math.max( 0, Math.min( 255, (int)( 255.0 * dimAlpha / MAX_DIM_ALPHA ) ) ) ;
            g2.setFont( MESSAGE_FONT ) ;
            FontMetrics fm = g2.getFontMetrics() ;
            int textWidth = fm.stringWidth( message ) ;
            int x = ( getWidth() - textWidth ) / 2 ;
            // Vertical position of the message baseline - 1/3 down from
            // the top, above where most bursts land. Raise/lower the
            // divisor to move it down/up.
            int y = getHeight() / 2 ;
            g2.setColor( new Color( messageColor.getRed(), messageColor.getGreen(), messageColor.getBlue(), textAlpha ) ) ;
            g2.drawString( message + " !!", x, y ) ;
        }
    }
}
