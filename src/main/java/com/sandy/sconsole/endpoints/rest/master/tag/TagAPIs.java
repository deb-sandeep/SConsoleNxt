package com.sandy.sconsole.endpoints.rest.master.tag;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.master.TagMaster;
import com.sandy.sconsole.dao.master.TagRecentUsage;
import com.sandy.sconsole.dao.master.Topic;
import com.sandy.sconsole.dao.master.repo.TagRecentUsageRepo;
import com.sandy.sconsole.dao.master.repo.TagRepo;
import com.sandy.sconsole.dao.master.repo.TopicRepo;
import com.sandy.sconsole.endpoints.rest.master.tag.helper.TagHelper;
import com.sandy.sconsole.endpoints.rest.master.tag.vo.TagVO;
import com.sandy.sconsole.endpoints.rest.master.tag.vo.reqres.TagCreateReq;
import com.sandy.sconsole.endpoints.rest.master.tag.vo.reqres.TagRenameReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sandy.sconsole.core.api.AR.*;

@Slf4j
@RestController
@RequestMapping( "/Master/Tag" )
public class TagAPIs {

    private static final int RECENT_TAG_LIMIT    = 10 ;
    private static final int MOST_USED_TAG_LIMIT = 10 ;

    @Autowired private TagRepo             tagRepo             = null ;
    @Autowired private TopicRepo           topicRepo           = null ;
    @Autowired private TagRecentUsageRepo  tagRecentUsageRepo  = null ;

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
            Map<Integer, Integer> associationCounts = new HashMap<>() ;
            for( TagRepo.TagAssociationCount c : tagRepo.findAssociationCountsByTopicId( topicId ) ) {
                associationCounts.put( c.getTagId(), c.getAssociationCount() ) ;
            }

            List<TagVO> vos = toVOs( tagRepo.findByTopicId( topicId ) ) ;
            vos.forEach( vo -> vo.setAssociationCount( associationCounts.getOrDefault( vo.getId(), 0 ) ) ) ;

            return success( vos ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    @GetMapping( "/Search" )
    public ResponseEntity<AR<List<TagVO>>> searchTags( @RequestParam( "text" ) String text ) {
        try {
            return success( toVOs( tagRepo.searchByText( TagHelper.normalize( text ) ) ) ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    @GetMapping( "/Recent" )
    public ResponseEntity<AR<List<TagVO>>> getRecentTags() {
        try {
            List<Integer> ids = tagRecentUsageRepo.findAllByOrderByLastUsedAtDesc().stream()
                    .map( TagRecentUsage::getTagId )
                    .limit( RECENT_TAG_LIMIT )
                    .toList() ;
            return success( toOrderedVOs( ids ) ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    @GetMapping( "/MostUsed" )
    public ResponseEntity<AR<List<TagVO>>> getMostUsedTags() {
        try {
            List<Integer> ids = tagRepo.findMostUsedTagIds( MOST_USED_TAG_LIMIT ) ;
            return success( toOrderedVOs( ids ) ) ;
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

    private List<TagVO> toOrderedVOs( List<Integer> orderedIds ) {
        Map<Integer, TagMaster> tagsById = new HashMap<>() ;
        tagRepo.findAllById( orderedIds ).forEach( t -> tagsById.put( t.getId(), t ) ) ;

        List<TagVO> vos = new ArrayList<>() ;
        for( Integer id : orderedIds ) {
            TagMaster tag = tagsById.get( id ) ;
            if( tag != null ) {
                vos.add( new TagVO( tag ) ) ;
            }
        }
        return vos ;
    }
}
