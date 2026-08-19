package com.sandy.sconsole.endpoints.rest.master.core;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.master.TagMaster;
import com.sandy.sconsole.dao.master.Topic;
import com.sandy.sconsole.dao.master.repo.TagRepo;
import com.sandy.sconsole.dao.master.repo.TopicRepo;
import com.sandy.sconsole.endpoints.rest.master.core.helper.TagHelper;
import com.sandy.sconsole.endpoints.rest.master.core.vo.TagVO;
import com.sandy.sconsole.endpoints.rest.master.core.vo.reqres.TagCreateReq;
import com.sandy.sconsole.endpoints.rest.master.core.vo.reqres.TagRenameReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.sandy.sconsole.core.api.AR.*;

@Slf4j
@RestController
@RequestMapping( "/Master/Tag" )
public class TagAPIs {

    @Autowired private TagRepo   tagRepo   = null ;
    @Autowired private TopicRepo topicRepo = null ;

    @PostMapping( "" )
    public ResponseEntity<AR<TagVO>> createTag( @RequestBody TagCreateReq req ) {
        try {
            TagHelper helper = SConsole.getBean( TagHelper.class ) ;
            return success( helper.createTag( req ) ) ;
        }
        catch( IllegalArgumentException e ) {
            return badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    @GetMapping( "/{tagId}" )
    public ResponseEntity<AR<TagVO>> getTag( @PathVariable Integer tagId ) {
        try {
            return success( new TagVO( tagRepo.findById( tagId ).get() ) ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    @GetMapping( "/All" )
    public ResponseEntity<AR<List<TagVO>>> getAllTags() {
        try {
            return success( toVOs( tagRepo.findAllByOrderByTagTextAsc() ) ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    @GetMapping( "/Topic/{topicId}" )
    public ResponseEntity<AR<List<TagVO>>> getTagsForTopic( @PathVariable Integer topicId ) {
        try {
            return success( toVOs( tagRepo.findByTopicId( topicId ) ) ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    @GetMapping( "/Search" )
    public ResponseEntity<AR<List<TagVO>>> searchTags( @RequestParam( "text" ) String text ) {
        try {
            return success( toVOs( tagRepo.searchByText( text.trim().toLowerCase() ) ) ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    @PostMapping( "/{tagId}/Rename" )
    public ResponseEntity<AR<TagVO>> renameTag(
            @PathVariable Integer tagId, @RequestBody TagRenameReq req ) {
        try {
            TagHelper helper = SConsole.getBean( TagHelper.class ) ;
            return success( helper.renameTag( tagId, req ) ) ;
        }
        catch( IllegalArgumentException e ) {
            return badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    @PostMapping( "/{tagId}/Topic/{newTopicId}" )
    @Transactional
    public ResponseEntity<AR<String>> changeTopic(
            @PathVariable Integer tagId, @PathVariable Integer newTopicId ) {
        try {
            TagMaster tag = tagRepo.findById( tagId ).get() ;
            Topic topic = topicRepo.findById( newTopicId ).get() ;
            tag.setTopic( topic ) ;
            tagRepo.save( tag ) ;
            return success() ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    @DeleteMapping( "/{tagId}" )
    @Transactional
    public ResponseEntity<AR<String>> deleteTag( @PathVariable Integer tagId ) {
        try {
            tagRepo.deleteById( tagId ) ;
            return success() ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    @PostMapping( "/{sourceTagId}/MergeInto/{targetTagId}" )
    public ResponseEntity<AR<String>> mergeTags(
            @PathVariable Integer sourceTagId, @PathVariable Integer targetTagId ) {
        try {
            TagHelper helper = SConsole.getBean( TagHelper.class ) ;
            helper.mergeTags( sourceTagId, targetTagId ) ;
            return success() ;
        }
        catch( IllegalArgumentException e ) {
            return badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    private List<TagVO> toVOs( Iterable<TagMaster> tags ) {
        List<TagVO> vos = new ArrayList<>() ;
        tags.forEach( t -> vos.add( new TagVO( t ) ) ) ;
        return vos ;
    }
}
