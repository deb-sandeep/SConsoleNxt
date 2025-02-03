package com.sandy.sconsole.api.master;

import com.sandy.sconsole.api.master.vo.TopicVO;
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

import static com.sandy.sconsole.core.api.AR.success;
import static com.sandy.sconsole.core.api.AR.systemError;

@Slf4j
@RestController
@RequestMapping( "/Master/Topic" )
public class TopicAPIs {
    
    @Autowired
    private TopicRepo topicRepo = null ;
    
    @GetMapping( "/All" )
    public ResponseEntity<AR<List<TopicVO>>> getTopics(
            @PathParam ( "syllabusName" ) String syllabusName ) {
        try {
            List<TopicVO> voList ;
            if( syllabusName == null ) {
                voList = convertToDTO( topicRepo.findAll() ) ;
            }
            else {
                voList = convertToDTO( topicRepo.findTopics( syllabusName ) ) ;
            }
            return success( voList ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    private List<TopicVO> convertToDTO( Iterable<Topic> topics ) {
        List<TopicVO> dtos = new ArrayList<>() ;
        topics.forEach( t -> dtos.add( new TopicVO( t ) ) );
        return dtos ;
    }
}
