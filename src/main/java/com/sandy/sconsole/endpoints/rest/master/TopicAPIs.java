package com.sandy.sconsole.endpoints.rest.master;

import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.master.Topic;
import com.sandy.sconsole.dao.master.TopicProblem;
import com.sandy.sconsole.dao.master.dto.TopicVO;
import com.sandy.sconsole.dao.master.repo.TopicProblemRepo;
import com.sandy.sconsole.dao.master.repo.TopicRepo;
import com.sandy.sconsole.endpoints.rest.master.vo.TopicProblemCountVO;
import jakarta.websocket.server.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sandy.sconsole.core.api.AR.success;
import static com.sandy.sconsole.core.api.AR.systemError;
import static com.sandy.sconsole.dao.master.repo.TopicRepo.TopicProblemTypeCount;

@Slf4j
@RestController
@RequestMapping( "/Master/Topic" )
public class TopicAPIs {
    
    @Autowired private TopicRepo        topicRepo = null ;
    @Autowired private TopicProblemRepo topicProblemRepo;
    
    @GetMapping( "/{topicId}" )
    public ResponseEntity<AR<TopicVO>> getTopic( @PathVariable( "topicId" ) int topicId ) {
        try {
            return success( new TopicVO( topicRepo.findById( topicId ).get() ) ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
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
    
    @GetMapping( "/ProblemTypeCounts" )
    public ResponseEntity<AR<List<TopicProblemCountVO>>> getProblemCounts() {
        
        try {
            List<TopicProblemCountVO> response = new ArrayList<>() ;
            TopicProblemCountVO lastVO   = null ;
            
            Map<Integer, TopicProblemCountVO> tpcMap = new HashMap<>() ;
            
            for( TopicProblemTypeCount count : topicRepo.getTopicProblemCounts() ) {
                if( lastVO == null || count.getTopicId() != lastVO.getTopicId() ) {
                    lastVO = new TopicProblemCountVO( count.getTopicId() ) ;
                    tpcMap.put( lastVO.getTopicId(), lastVO ) ;
                    response.add( lastVO ) ;
                }
                if( count.getProblemType() != null ) {
                    lastVO.addCount( count.getProblemType(), count.getNumProblems() ) ;
                }
            }
            
            for( TopicProblemTypeCount remainingCount : topicRepo.getRemainingTopicProblemCounts() ) {
                TopicProblemCountVO vo = tpcMap.get( remainingCount.getTopicId() ) ;
                vo.addRemainingCount( remainingCount.getProblemType(), remainingCount.getNumProblems() ) ;
            }
            
            return success( response ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @GetMapping( "/{topicId}/Problems" )
    public ResponseEntity<AR<List<TopicProblem>>> getProblems( @PathVariable( "topicId" ) int topicId ) {
        
        try {
            return success( topicProblemRepo.findByTopicId( topicId ) ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
}
