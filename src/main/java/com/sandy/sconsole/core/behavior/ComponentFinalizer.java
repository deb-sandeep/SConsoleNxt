package com.sandy.sconsole.core.behavior;

import com.sandy.sconsole.SConsole;

/**
 * Spring beans implementing this interface are called upon during the
 * shutdown of SConsole.
 */
public interface ComponentFinalizer {

    default boolean isInvocable() { return true ; }

    void destroy( SConsole app ) throws Exception ;
}
