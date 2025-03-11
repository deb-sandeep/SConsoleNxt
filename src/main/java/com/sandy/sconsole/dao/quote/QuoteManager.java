package com.sandy.sconsole.dao.quote;

import com.sandy.sconsole.SConsole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;

@Slf4j
@Component
public class QuoteManager {

    private static final int MAX_QUOTE_LEN = 100 ;

    @Autowired private QuoteRepo quoteRepo ;

    private final List<Quote> allQuotes = new ArrayList<>() ;
    private final Map<String, List<Quote>> quotesBySpeaker = new HashMap<>() ;
    private final Map<String, List<Quote>> quotesBySection = new HashMap<>() ;

    public void initialize( SConsole app ) {

        for( Quote quote : quoteRepo.findAll() ) {
            if( quote.getQuote().length() <= MAX_QUOTE_LEN ) {
                allQuotes.add( quote ) ;
                quotesBySpeaker.computeIfAbsent( quote.getSpeaker(), s -> new ArrayList<>() ).add( quote ) ;
                quotesBySection.computeIfAbsent( quote.getSection(), s -> new ArrayList<>() ).add( quote ) ;
            }
        }
        sortAllQuotes() ;
    }

    private void refreshQuote( Quote quote ) {
        replaceOrAdd( quote, allQuotes ) ;
        replaceOrAdd( quote, quotesBySpeaker.computeIfAbsent(
                                                    quote.getSpeaker(),
                                                    s -> new ArrayList<>() ) ) ;
        replaceOrAdd( quote, quotesBySection.computeIfAbsent(
                                                    quote.getSection(),
                                                    s -> new ArrayList<>() ) ) ;
        sortAllQuotes() ;
    }

    private void sortAllQuotes() {
        allQuotes.sort( Comparator.comparingInt( Quote::getNumShows ) ) ;
    }

    private void replaceOrAdd( Quote quote, List<Quote> quotes ) {
        for( int i=0; i<quotes.size(); i++ ) {
            if( Objects.equals( quotes.get( i ).getId(), quote.getId() ) ) {
                quotes.remove( i ) ;
                break ;
            }
        }
        quotes.add( quote ) ;
    }

    public Quote getNextRandomQuote() {
        return allQuotes.get( new Random().nextInt( 50 ) ) ;
    }

    public void incrementViewCount( Quote quote ) {
        quote.setNumShows( quote.getNumShows()+1 ) ;
        quote.setLastDisplayTime( new Timestamp( System.currentTimeMillis() ) ) ;
        quote = quoteRepo.save( quote ) ;
        refreshQuote( quote ) ;
    }
}
