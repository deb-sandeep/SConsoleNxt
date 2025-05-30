package com.sandy.sconsole.endpoints.rest.master;

import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.dao.master.TopicTrackAssignment;
import com.sandy.sconsole.dao.master.Track;
import com.sandy.sconsole.dao.master.repo.TopicTrackAssignmentRepo;
import com.sandy.sconsole.dao.master.repo.TrackRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.sandy.sconsole.EventCatalog.TRACK_UPDATED;

@Slf4j
@RestController
@RequestMapping( "/Master/Track" )
@Transactional
public class TrackAPIs {
    
    @Autowired private EventBus eventBus;
    
    @Autowired private TrackRepo trackRepo = null ;
    @Autowired private TopicTrackAssignmentRepo ttaRepo = null ;
    
    @GetMapping( "/All" )
    public ResponseEntity<AR<List<Track>>> getAllSyllabus() {
        try {
            return AR.success( new ArrayList<>( trackRepo.findAllByOrderByIdAsc() ) ) ;
        }
        catch( Exception e ) {
            return AR.systemError( e ) ;
        }
    }
    
    @GetMapping( "/CurrentTopicAssignments" )
    public ResponseEntity<AR<List<TopicTrackAssignment>>> getCurrentTopicAssignments(
            @RequestParam( "date" ) @DateTimeFormat( iso = DateTimeFormat.ISO.DATE ) Date date ) {
        try {
            if( date == null ) {
                date = new Date() ;
            }
            List<TopicTrackAssignment> trackAssignments = ttaRepo.findActiveAssignments( date ) ;
            return AR.success( trackAssignments ) ;
        }
        catch( Exception e ) {
            return AR.systemError( e ) ;
        }
    }
    
    @PostMapping( "/{id}/SaveTopicSchedules" )
    public ResponseEntity<AR<List<TopicTrackAssignment>>> saveTopicSchedules(
                            @PathVariable("id") int trackId,
                            @RequestBody List<TopicTrackAssignment> schedules ) {
        try {
            log.debug( "Saving track assignments for track id {}", trackId ) ;
            
            // First we delete all topics belonging to the given track
            ttaRepo.deleteByTrackId( trackId ) ;
            
            // Then we delete all assignments for the given topic. This is
            // important as some of the topics n this track might have been
            // moved from other tracks. So the earlier track assignments need
            // to be deleted.
            ttaRepo.deleteByTopicId( schedules.stream()
                                              .map( TopicTrackAssignment::getTopicId )
                                              .collect( Collectors.toList() ) ) ;
            
            schedules.forEach( schedule -> {
                // Null the id so that a new entry will be created
                schedule.setId( null ) ;
                ttaRepo.save( schedule ) ;
                log.debug( "Saved track assignment {}", schedule ) ;
            }) ;
            
            ttaRepo.flush() ;
            
            log.debug( "New assignment saved for track id {}", trackId ) ;
            log.debug( "Publishing TRACK_UPDATED event for track id {}", trackId ) ;
            
            Track savedTrack = trackRepo.findById( trackId ).get() ;
            eventBus.publishEvent( TRACK_UPDATED, trackId ) ;
            
            return AR.success( new ArrayList<>( savedTrack.getAssignedTopics() ) ) ;
        }
        catch( Exception e ) {
            return AR.systemError( e ) ;
        }
    }
}
