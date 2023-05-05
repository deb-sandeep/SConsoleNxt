package com.sandy.sconsole.screen.screens.qotd;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.behavior.ComponentInitializer;
import com.sandy.sconsole.dao.quote.Quote;
import com.sandy.sconsole.dao.quote.QuoteRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class QOTDManager implements ComponentInitializer {

    private static final int MAX_QUOTE_LEN = 100 ;

    @Autowired private QuoteRepo quoteRepo ;

    private final List<Quote> allQuotes = new ArrayList<>() ;
    private final Map<String, List<Quote>> quotesBySpeaker = new HashMap<>() ;
    private final Map<String, List<Quote>> quotesBySection = new HashMap<>() ;

    @Override
    public void initialize( SConsole app ) {

        log.debug( "Initializing QOTDManager." ) ;
        for( Quote quote : quoteRepo.findAll() ) {
            if( quote.getQuote().length() <= MAX_QUOTE_LEN ) {
                allQuotes.add( quote ) ;
                quotesBySpeaker.computeIfAbsent( quote.getSpeaker(), s -> new ArrayList<>() ).add( quote ) ;
                quotesBySection.computeIfAbsent( quote.getSection(), s -> new ArrayList<>() ).add( quote ) ;
            }
        }
        log.debug( "  {} quotes loaded.", allQuotes.size() ) ;
    }

    public Quote getNextRandomQuote() {
        return allQuotes.get( new Random().nextInt( allQuotes.size() ) ) ;
    }
}
