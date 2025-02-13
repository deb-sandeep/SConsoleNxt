package com.sandy.sconsole.api.master;

import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.master.TopicTrackAssignment;
import com.sandy.sconsole.dao.master.Track;
import com.sandy.sconsole.dao.master.repo.TopicTrackAssignmentRepo;
import com.sandy.sconsole.dao.master.repo.TrackRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping( "/Master/Track" )
public class TrackAPIs {
    
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
    
    @PostMapping( "/{id}/SaveTopicSchedules" )
    @Transactional
    public ResponseEntity<AR<List<TopicTrackAssignment>>> saveTopicSchedules(
                            @PathVariable("id") int trackId,
                            @RequestBody List<TopicTrackAssignment> schedules ) {
        try {
            ttaRepo.deleteByTrackId( trackId ) ;
            schedules.forEach( schedule -> {
                schedule.setId( null ) ;
                ttaRepo.save( schedule ) ;
            }) ;
            
            Track savedTrack = trackRepo.findById( trackId ).get() ;
            return AR.success( new ArrayList<>( savedTrack.getAssignedTopics() ) ) ;
        }
        catch( Exception e ) {
            return AR.systemError( e ) ;
        }
    }
}
