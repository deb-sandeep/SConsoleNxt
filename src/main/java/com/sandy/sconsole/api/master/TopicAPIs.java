package com.sandy.sconsole.api.master;

import com.sandy.sconsole.api.master.dto.TopicDTO;
import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.master.Topic;
import com.sandy.sconsole.dao.master.repo.TopicRepo;
import jakarta.websocket.server.PathParam;
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
@RequestMapping( "/Master/Topic" )
public class TopicAPIs {
    
    @Autowired
    private TopicRepo topicRepo = null ;
    
    @GetMapping( "/All" )
    public ResponseEntity<AR<List<TopicDTO>>> getTopics(
            @PathParam ( "syllabusName" ) String syllabusName ) {
        try {
            List<TopicDTO> dtos ;
            if( syllabusName == null ) {
                dtos = convertToDTO( topicRepo.findAll() ) ;
            }
            else {
                dtos = convertToDTO( topicRepo.findTopics( syllabusName ) ) ;
            }
            return AR.success( dtos ) ;
        }
        catch( Exception e ) {
            return AR.systemError( e ) ;
        }
    }
    
    private List<TopicDTO> convertToDTO( Iterable<Topic> topics ) {
        List<TopicDTO> dtos = new ArrayList<>() ;
        topics.forEach( t -> dtos.add( new TopicDTO( t ) ) );
        return dtos ;
    }
}
