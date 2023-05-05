package com.sandy.sconsole.core.behavior;

import com.sandy.sconsole.SConsole;

/**
 * Spring beans implementing this interface are called upon during system
 * bootup to delegate parts of system initialization.
 */
public interface ComponentInitializer {

    default boolean isInvocable() { return true ; }

    /** Initializers are called upon in ascending order of their preference. */
    default int getInitializationSequencePreference() { return 0 ; }

    void initialize( SConsole app ) throws Exception ;
}
