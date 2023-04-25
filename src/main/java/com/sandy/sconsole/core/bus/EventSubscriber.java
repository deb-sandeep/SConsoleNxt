package com.sandy.sconsole.core.bus;

/**
 * An interface which specifies the contract that every event subscriber has
 * to implement to be able to register itself with the {@link EventBus}.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public interface EventSubscriber {

    /**
     * This method should be implemented by all the event subscribers to handle
     * the event notifications from the event bus. The event bus calls on this
     * method for each registered subscriber interested in listening to the
     * event type specified. The order of invocation across multiple subscribers
     * is not guaranteed.
     * <p>
     * Please note that the value parameter is shared between all the subscribers
     * and hence it is not recommended that the subscribers modify the state
     * of the event value.
     * <p>
     * Also, please note that the invocation may or may not be in the same 
     * thread as the event publisher. This will depend upon the type of 
     * subscriber registration (synchronous or asynchronous) with the EventBus 
     *
     * @param event The event which has been published, never null.
     */
    void handleEvent( final Event event ) ;
}
