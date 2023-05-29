package com.sandy.sconsole.test.core.clock;

import com.sandy.sconsole.core.clock.SConsoleClock;
import com.sandy.sconsole.core.clock.ScheduledTask;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
public class SConsoleClockSchedulerTest {

    private SConsoleClock clock = null ;

    @BeforeEach void setUp() {
        clock = new SConsoleClock() ;
        clock.initialize() ;
    }

    @AfterEach void tearDown() {
        clock.stopClock() ;
    }

    @Test void schedulerTest() throws Exception {

        final List<String> callLog = new ArrayList<>() ;
        clock.scheduleTask(
                "* * * * *",
                new ScheduledTask() {
                    @Override protected void executeTask() {
                        callLog.add( "Task called." ) ;
                    }
                }
        ) ;
        TimeUnit.MINUTES.sleep( 1 ) ;
        assertThat( callLog.size(), is( greaterThan( 0 ) ) ) ;
    }
}