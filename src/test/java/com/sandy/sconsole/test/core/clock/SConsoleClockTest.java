package com.sandy.sconsole.test.core.clock;

import com.sandy.sconsole.core.clock.SConsoleClock;
import com.sandy.sconsole.test.core.clock.mock.MockClockTickListener;
import com.sandy.sconsole.test.core.clock.mock.MockRolloverTimeProvider;
import org.jfree.data.time.Day;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class SConsoleClockTest {

    private SConsoleClock clock = null ;

    @BeforeEach void setUp() {
        clock = new SConsoleClock() ;
    }

    @AfterEach void tearDown() {
        clock.stopClock() ;
    }

    @Test void uninitializedClock() {
        assertNull( clock.getToday() ) ;
    }

    @Test
    public void initializedClock() {
        clock.initialize() ;
        Day today = new Day( new Date() ) ;
        assertEquals( today, clock.getToday() ) ;
        assertEquals( today.getFirstMillisecond(), clock.getTodayFirstMillis() ) ;
        assertEquals( today.getLastMillisecond(), clock.getTodayLastMillis() ) ;
    }

    @Test
    public void secondTick() throws Exception {

        MockClockTickListener l = new MockClockTickListener() ;
        clock.addTickListener( l, TimeUnit.SECONDS ) ;
        clock.initialize() ;

        Thread.sleep( 2000 ) ;
        clock.removeTickListener( l ) ;

        assertTrue( l.getNumTicksRecd() == 2 || l.getNumTicksRecd() == 3 ); ;

        // Once a tick listener is removed, it no longer receives callbacks
        Thread.sleep( 1000 ) ;
        assertEquals( 2, l.getNumTicksRecd() ) ;
    }

    @Test
    public void dayTick() throws Exception {

        MockClockTickListener l = new MockClockTickListener() ;
        clock.addTickListener( l, TimeUnit.DAYS ) ;
        clock.setCurrentTimeProvider( new MockRolloverTimeProvider( TimeUnit.DAYS ) ) ;
        clock.initialize() ;

        Thread.sleep( 2000 ) ;
        assertEquals( 1, l.getNumTicksRecd() ) ;
    }

    @Test
    public void hourTick() throws Exception {

        MockClockTickListener l = new MockClockTickListener() ;
        clock.addTickListener( l, TimeUnit.HOURS ) ;
        clock.setCurrentTimeProvider( new MockRolloverTimeProvider( TimeUnit.HOURS ) ) ;
        clock.initialize() ;

        Thread.sleep( 2000 ) ;
        assertEquals( 1, l.getNumTicksRecd() ) ;
    }
}
