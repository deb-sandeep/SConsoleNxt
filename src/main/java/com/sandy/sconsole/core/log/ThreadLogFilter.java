package com.sandy.sconsole.core.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;

public class ThreadLogFilter extends Filter<ILoggingEvent> {

    private final List<String> includedThreadNames = new ArrayList<>() ;

    public void addIncludedThreadName( String name ) {
        this.includedThreadNames.add( name ) ;
    }

    @Override
    public FilterReply decide( ILoggingEvent event ) {

        String threadName = MDC.get( LogIndenter.THREAD_NAME_KEY ) ;
        if( threadName == null || threadName.equals( "main" ) ) {
            return FilterReply.ACCEPT ;
        }
        else if( includedThreadNames.contains( threadName ) ) {
            return FilterReply.ACCEPT ;
        }
        return FilterReply.DENY ;
    }
}
