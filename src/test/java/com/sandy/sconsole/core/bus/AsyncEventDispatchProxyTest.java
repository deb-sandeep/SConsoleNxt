package com.sandy.sconsole.core.bus;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AsyncEventDispatchProxyTest {

    @Test
    void dispatchesEachQueuedEventWithItsOwnPayload() {

        RecordingSubscriber subscriber = new RecordingSubscriber() ;
        ManualExecutorService executor = new ManualExecutorService() ;
        AsyncEventDispatchProxy proxy = new AsyncEventDispatchProxy( subscriber, executor ) ;

        proxy.handleEvent( new Event( 101, "start" ) ) ;
        proxy.handleEvent( new Event( 102, "end" ) ) ;

        executor.runAll() ;

        assertEquals( List.of( 101, 102 ), subscriber.eventIds ) ;
        assertEquals( List.of( "start", "end" ), subscriber.values ) ;
    }

    private static class RecordingSubscriber implements EventSubscriber {

        private final List<Integer> eventIds = new ArrayList<>() ;
        private final List<Object> values = new ArrayList<>() ;

        @Override
        public void handleEvent( Event event ) {
            eventIds.add( event.getEventId() ) ;
            values.add( event.getValue() ) ;
        }
    }

    private static class ManualExecutorService extends AbstractExecutorService {

        private final List<Runnable> queuedTasks = new ArrayList<>() ;
        private boolean shutdown = false ;

        @Override
        public void shutdown() {
            shutdown = true ;
        }

        @Override
        public List<Runnable> shutdownNow() {
            shutdown = true ;
            List<Runnable> pendingTasks = new ArrayList<>( queuedTasks ) ;
            queuedTasks.clear() ;
            return pendingTasks ;
        }

        @Override
        public boolean isShutdown() {
            return shutdown ;
        }

        @Override
        public boolean isTerminated() {
            return shutdown && queuedTasks.isEmpty() ;
        }

        @Override
        public boolean awaitTermination( long timeout, TimeUnit unit ) {
            return isTerminated() ;
        }

        @Override
        public void execute( Runnable command ) {
            queuedTasks.add( command ) ;
        }

        private void runAll() {
            List<Runnable> tasks = new ArrayList<>( queuedTasks ) ;
            queuedTasks.clear() ;
            tasks.forEach( Runnable::run ) ;
        }
    }
}
