package com.sandy.sconsole.core.log;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

public class LogIndenter extends MessageConverter {

    public static final String THREAD_NAME_KEY = "threadName" ;

    private static final String INDENT = "  " ;

    private static final String SM_ADD_INDENT      = "> " ;
    private static final String SM_ADD_DBLINDENT   = ">> " ;
    private static final String SM_REMOVE_INDENT   = "< " ;
    private static final String SM_REMOVE_DBLINDENT= "<< " ;
    private static final String SM_USE_INDENT      = "- " ;
    private static final String SM_USE_INDENT2     = "-> " ;
    private static final String SM_USE_INDENT3     = "->> " ;
    private static final String SM_RESET_INDENT    = "<<< " ;

    private static final String EM_ADD_INDENT      = " >" ;
    private static final String EM_ADD_DBLINDENT   = " >>" ;
    private static final String EM_REMOVE_INDENT   = " <" ;
    private static final String EM_REMOVE_DBLINDENT= " <<" ;
    private static final String EM_RESET_INDENT    = " <<<" ;

    private static final String[] SM_MARKERS = {
            SM_ADD_INDENT,
            SM_ADD_DBLINDENT,
            SM_REMOVE_INDENT,
            SM_REMOVE_DBLINDENT,
            SM_USE_INDENT,
            SM_USE_INDENT2,
            SM_USE_INDENT3,
            SM_RESET_INDENT
    } ;
    
    private static final String[] EM_MARKERS = {
            EM_ADD_INDENT,
            EM_ADD_DBLINDENT,
            EM_REMOVE_INDENT,
            EM_REMOVE_DBLINDENT,
            EM_RESET_INDENT
    } ;

    public String convert( ILoggingEvent event) {

        String formattedMsg = super.convert( event ) ;
        String[] strippedMsg = stripMessage( formattedMsg ) ;
        String msgPrefix = processStartMarker( strippedMsg[0] ) ;
        processEndMarker( strippedMsg[1] ) ;
        
        return msgPrefix + strippedMsg[2] ;
    }

    private String[] stripMessage( String formattedMsg ) {
        String startMarker = getStartMarker( formattedMsg ) ;
        String endMarker = getEndMarker( formattedMsg ) ;
        String strippedMsg = formattedMsg ;
        
        if( startMarker != null ) {
            strippedMsg = strippedMsg.substring( startMarker.length() ) ;
        }
        
        if( endMarker != null ) {
            strippedMsg = strippedMsg.substring( 0, strippedMsg.length()-endMarker.length() ) ;
        }
        return new String[]{ startMarker, endMarker, strippedMsg } ;
    }
    
    private String getStartMarker( String formattedMsg ) {
        for( String marker : SM_MARKERS ) {
            if( formattedMsg.startsWith( marker ) ) {
                return marker ;
            }
        }
        return null ;
    }

    private String getEndMarker( String formattedMsg ) {
        formattedMsg = StringUtils.stripEnd( formattedMsg, " " ) ;
        for( String marker : EM_MARKERS ) {
            if( formattedMsg.endsWith( marker ) ) {
                return marker ;
            }
        }
        return null ;
    }

    private String processStartMarker( String marker ) {
        String prefix = "" ;
        if( marker != null ) {
            switch( marker ) {
                case SM_ADD_INDENT       -> prefix = addIndent( 1 ) ;
                case SM_ADD_DBLINDENT    -> prefix = addIndent( 2 ) ;
                case SM_REMOVE_INDENT    -> prefix = deIndent( 1 ) ;
                case SM_REMOVE_DBLINDENT -> prefix = deIndent( 2 ) ;
                case SM_USE_INDENT       -> prefix = getCurrentIndent();
                case SM_USE_INDENT2      -> prefix = getCurrentIndent() + INDENT;
                case SM_USE_INDENT3      -> prefix = getCurrentIndent() + INDENT + INDENT;
                case SM_RESET_INDENT     -> prefix = resetIndent();
            }
        }
        return prefix ;
    }

    private void processEndMarker( String marker ) {
        if( marker != null ) {
            switch( marker ) {
                case EM_ADD_INDENT       -> addIndent( 1 );
                case EM_ADD_DBLINDENT    -> addIndent( 2 );
                case EM_REMOVE_INDENT    -> deIndent( 1 );
                case EM_REMOVE_DBLINDENT -> deIndent( 2 );
                case EM_RESET_INDENT     -> resetIndent();
            }
        }
    }

    private String addIndent( int numIndent ) {
        String curIndent = getCurrentIndent() ;
        curIndent += StringUtils.repeat( INDENT, numIndent ) ;
        MDC.put( getIndentKey(), curIndent ) ;
        return curIndent ;
    }

    private String deIndent( int numIndent ) {
        String reqIndent = StringUtils.repeat( INDENT, numIndent ) ;
        String curIndent = getCurrentIndent() ;
        if( curIndent.length() < reqIndent.length() ) {
            curIndent = "" ;
        }
        else {
            curIndent = curIndent.substring( 0, curIndent.length()-reqIndent.length() ) ;
        }
        MDC.put( getIndentKey(), curIndent ) ;
        return curIndent ;
    }

    private static String getCurrentIndent() {
        String curIndent = MDC.get( getIndentKey() ) ;
        if( curIndent == null ) {
            curIndent = "" ;
        }
        return curIndent ;
    }

    public static String resetIndent() {
        MDC.remove( getIndentKey() ) ;
        return "" ;
    }

    private static String getIndentKey() {
        String threadName = MDC.get( LogIndenter.THREAD_NAME_KEY ) ;
        if( threadName == null ) {
            return "LogIndenter.mainThreadLogIndentKey" ;
        }
        return "LogIndenter." + threadName + "ThreadLogIndentKey" ;
    }
}
