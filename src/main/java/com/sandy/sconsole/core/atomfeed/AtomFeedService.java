package com.sandy.sconsole.core.atomfeed;

import com.rometools.rome.feed.atom.Content;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.io.WireFeedOutput;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.core.clock.ClockTickListener;
import com.sandy.sconsole.dao.audit.EventLog;
import com.sandy.sconsole.dao.audit.repo.EventLogRepo;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class AtomFeedService implements ClockTickListener {
    
    private final List<EventLog> events = new ArrayList<>() ;
    
    @Autowired EventBus eventBus ;
    @Autowired EventLogRepo eventLogRepo ;
    
    private final AtomicBoolean needsFeedGeneration = new AtomicBoolean( true ) ;
    private Feed feed ;
    private String feedXML ;
    
    @PostConstruct
    public void init() {
        events.addAll( eventLogRepo.findEventLogsForToday() ) ;
    }
    
    @Override
    public void dayTicked( Calendar calendar ) {
        events.clear() ;
    }
    
    public void addFeedEvent( String msgType, String title, String msgFormat, Object... args ) {
        
        EventLog eventLog = new EventLog() ;
        eventLog.setMessageType( msgType ) ;
        eventLog.setTitle( title ) ;
        eventLog.setMessage( String.format( msgFormat, args ) ) ;
        eventLog.setTime( new Date() ) ;
        
        EventLog savedLog = eventLogRepo.save( eventLog ) ;
        eventLog.setId( savedLog.getId() ) ;
        
        events.add( eventLog ) ;
        needsFeedGeneration.set( true ) ;
    }
    
    public String getFeed() throws Exception {
        
        if( needsFeedGeneration.get() ) {
            Feed feed = new Feed();
            feed.setFeedType( "atom_1.0" ) ;
            feed.setTitle( "SConsole" ) ;
            feed.setUpdated( new Date() ) ;
            feed.setId( "urn:feed:sconsole" ) ;
            
            List<Entry> entries = new ArrayList<>() ;
            
            for( EventLog e : events ) {
                entries.add( getEntry( e ) ) ;
            }
            
            feed.setEntries( entries ) ;
            WireFeedOutput output = new WireFeedOutput();
            feedXML = output.outputString(feed);
            
            needsFeedGeneration.set( false ) ;
        }
        return feedXML;
    }
    
    private static Entry getEntry( EventLog e ) {
        Entry entry = new Entry() ;
        entry.setTitle( e.getTitle() ) ;
        entry.setId( "urn:uuid:" + e.getId() ) ;
        entry.setPublished( e.getTime() ) ;
        entry.setUpdated( e.getTime() ) ;
        
        Content content = new Content();
        content.setType( e.getMessageType() ) ;
        content.setValue( e.getMessage() ) ;
        
        entry.setContents( List.of(content) ) ;
        return entry;
    }
}
