package com.sandy.sconsole.core.bus;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention( RetentionPolicy.RUNTIME )
public @interface Payload {
    Class<?> value() ;
}
