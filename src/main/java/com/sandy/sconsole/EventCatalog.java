package com.sandy.sconsole;

public class EventCatalog {

    public @interface Payload {
        Class<?> type() ;
    }

    public static final int CORE_EVENT_RANGE_MIN = 100 ;
    public static final int CORE_EVENT_RANGE_MAX = 200 ;

    // =============== Core Events [Start] =====================================
    
    // --------------- Core Events [End] ---------------------------------------
}
