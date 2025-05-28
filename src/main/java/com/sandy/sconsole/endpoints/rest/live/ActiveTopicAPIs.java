package com.sandy.sconsole.endpoints.rest.live;

import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.master.TopicProblem;
import com.sandy.sconsole.dao.master.repo.TopicProblemRepo;
import com.sandy.sconsole.dao.master.repo.TopicRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.sandy.sconsole.core.api.AR.success;
import static com.sandy.sconsole.core.api.AR.systemError;

@Slf4j
@RestController
@RequestMapping( "/Topic" )
@Transactional
public class ActiveTopicAPIs {
    
    @Autowired private TopicProblemRepo tpRepo ;
    @Autowired private TopicRepo topicRepo ;
    
    @GetMapping( "/{topicId}/ActiveProblems" )
    public ResponseEntity<AR<List<TopicProblem>>> getActiveProblems( @PathVariable( "topicId" ) int topicId ) {
        try {
            return success( tpRepo.findActiveProblems( topicId )) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
}
