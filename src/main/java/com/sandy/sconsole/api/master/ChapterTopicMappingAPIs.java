package com.sandy.sconsole.api.master;

import com.sandy.sconsole.api.master.helper.TopicMappingHelper;
import com.sandy.sconsole.api.master.vo.reqres.ChapterTopicMappingReq;
import com.sandy.sconsole.core.api.AR;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.sandy.sconsole.core.api.AR.*;

@Slf4j
@RestController
@RequestMapping( "/Master/ChapterTopicMapping" )
public class ChapterTopicMappingAPIs {
    
    @Autowired private TopicMappingHelper helper = null ;
    
    @PostMapping( "" )
    public ResponseEntity<AR<Integer>> createOrUpdateChapterTopicMapping(
            @RequestBody ChapterTopicMappingReq mappingReq ) {
        
        try {
            int mappingId = helper.createOrUpdateMapping( mappingReq ) ;
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
            helper.deleteMapping( mapId ) ;
            return success( "Chapter topic mapping deleted successfully" );
        }
        catch( Exception e ) {
            return systemError( e );
        }
    }
    
}
