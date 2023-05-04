package com.sandy.sconsole.core.behavior;

import com.sandy.sconsole.SConsole;

/**
 * Spring beans implementing this interface are called upon during system
 * bootup to delegate parts of system initialization.
 */
public interface SystemInitializer {

    default boolean isInvocable() { return true ; }

    void initialize( SConsole app ) throws Exception ;
}
