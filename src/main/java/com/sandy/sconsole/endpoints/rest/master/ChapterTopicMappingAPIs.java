package com.sandy.sconsole.endpoints.rest.master;

import com.sandy.sconsole.endpoints.rest.master.helper.TopicMappingHelper;
import com.sandy.sconsole.endpoints.rest.master.vo.TopicChapterMappingVO;
import com.sandy.sconsole.endpoints.rest.master.vo.reqres.ChapterTopicMappingReq;
import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.master.TopicChapterMap;
import com.sandy.sconsole.dao.master.repo.TopicChapterMapRepo;
import jakarta.websocket.server.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.sandy.sconsole.core.api.AR.*;

@Slf4j
@RestController
@RequestMapping( "/Master/ChapterTopicMapping" )
public class ChapterTopicMappingAPIs {
    
    @Autowired private TopicChapterMapRepo tcmRepo = null ;
    
    @Autowired private TopicMappingHelper helper = null ;
    
    @PostMapping( "" )
    public ResponseEntity<AR<Integer>> createOrUpdateChapterTopicMapping(
            @RequestBody ChapterTopicMappingReq mappingReq ) {
        
        try {
            int mappingId = helper.saveChapterTopicMapping( mappingReq ) ;
            return success( mappingId ) ;
        }
        catch( DataIntegrityViolationException dive ) {
            log.error( "Duplicate entry.", dive ) ;
            return functionalError( "Entry already exists", dive ) ;
         }
        catch( Exception e ) {
            return systemError( e );
        }
    }
    
    @DeleteMapping( "{mapId}" )
    public ResponseEntity<AR<String>> deleteChapterTopicMapping(
            @PathVariable( "mapId" ) Integer mapId ) {
        
        try {
            helper.deleteTopicChapterMapping( mapId ) ;
            return success( "Chapter topic mapping deleted successfully" );
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }
    
    @GetMapping( "" )
    public ResponseEntity<AR<List<TopicChapterMappingVO>>> getTopicChapterMappings(
            @PathParam( "syllabusName" ) String syllabusName ) {
        
        try {
            return success( helper.getTopicChapterMappings( syllabusName ) ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @PostMapping( "/SwapAttemptSequence/{mappingId1}/{mappingId2}" )
    public ResponseEntity<AR<String>> swapAttemptSequence(
            @PathVariable( "mappingId1" ) int mappingId1,
            @PathVariable( "mappingId2" ) int mappingId2 ) {
        
        try {
            TopicChapterMap map1 = tcmRepo.findById( mappingId1 ).get() ;
            TopicChapterMap map2 = tcmRepo.findById( mappingId2 ).get() ;
            
            int savedMap1AttemptSeq = map1.getAttemptSeq() ;
            
            map1.setAttemptSeq( map2.getAttemptSeq() ) ;
            map2.setAttemptSeq( savedMap1AttemptSeq ) ;
            
            tcmRepo.save( map1 ) ;
            tcmRepo.save( map2 ) ;
            
            return success( "Swapping of attempt sequence successful" ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @PostMapping( "/ToggleProblemMappingDone/{mappingId}" )
    public ResponseEntity<AR<String>> toggleProblemMappingDone(
            @PathVariable( "mappingId" ) int mappingId ) {
        
        try {
            TopicChapterMap map = tcmRepo.findById( mappingId ).get() ;
            map.setProblemMappingDone( !map.getProblemMappingDone() ) ;
            tcmRepo.save( map ) ;
            
            return success() ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
}
