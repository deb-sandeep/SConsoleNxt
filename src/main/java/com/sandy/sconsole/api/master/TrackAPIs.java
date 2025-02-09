package com.sandy.sconsole.api.master;

import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.master.Track;
import com.sandy.sconsole.dao.master.repo.TrackRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping( "/Master/Track" )
public class TrackAPIs {
    
    @Autowired
    private TrackRepo trackRepo = null ;
    
    @GetMapping( "/All" )
    public ResponseEntity<AR<List<Track>>> getAllSyllabus() {
        try {
            return AR.success( new ArrayList<>( trackRepo.findAllByOrderByIdAsc() ) ) ;
        }
        catch( Exception e ) {
            return AR.systemError( e ) ;
        }
    }
}
