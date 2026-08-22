package com.sandy.sconsole.endpoints.rest.master.tag;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.master.Tag;
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

    /**
     * Creates a new tag. The tag text is normalized (trimmed, lowercased, and
     * stripped of whitespace/'-'/'.'/':') to check for an existing tag with the
     * same normalized text; a match is rejected with a bad request rather than
     * silently creating a near-duplicate. The color is optional - if omitted
     * (null/blank), the new tag is created with no color.
     */
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

    /**
     * Fetches a single tag by id. associationCount is left at -1 (not
     * populated) on this endpoint's response.
     */
    @GetMapping( "/{tagId}" )
    public ResponseEntity<AR<TagVO>> getTag( @PathVariable Integer tagId ) {
        try {
            return success( new TagVO( tagRepo.findById( tagId ).get() ) ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    /**
     * Returns every tag in the system, sorted alphabetically by tag text.
     * associationCount is left at -1 (not populated) on this endpoint's
     * response.
     */
    @GetMapping( "/All" )
    public ResponseEntity<AR<List<TagVO>>> getAllTags() {
        try {
            return success( toVOs( tagRepo.findAllByOrderByTagTextAsc() ) ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    /**
     * Returns every tag whose dominant/home topic is topicId. This is the only
     * endpoint that populates associationCount on the returned TagVOs -
     * the total number of problems plus questions each tag is attached to,
     * across all topics/subjects, not just this one. All other endpoints in
     * this controller leave associationCount at its default of -1.
     */
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

    /**
     * Case/separator-insensitive "contains anywhere" search over tag text
     * (e.g. "law" matches "Grahams Law"). The search text is normalized the
     * same way as tag text is on create/rename, so a query containing spaces,
     * dashes, dots, or colons still matches correctly.
     */
    @GetMapping( "/Search" )
    public ResponseEntity<AR<List<TagVO>>> searchTags( @RequestParam( "text" ) String text ) {
        try {
            return success( toVOs( tagRepo.searchByText( TagHelper.normalize( text ) ) ) ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    /**
     * Returns up to RECENT_TAG_LIMIT tags, most-recently-attached-to-an-item
     * first. Backed by the tag_recent_usage rolling cache, which is updated
     * whenever a tag is attached to a problem/question (not on tag creation
     * alone) - see TagAssociationHelper.addTag.
     */
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

    /**
     * Returns up to MOST_USED_TAG_LIMIT tags, ordered by total number of
     * problem+question associations (descending, tag text as tie-break).
     * Computed live from the association tables on every call - there is no
     * stored/cached ranking to keep in sync.
     */
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

    /**
     * Renames a tag's display text. Subject to the same normalized-uniqueness
     * check as create - renaming to text that normalizes the same as another
     * existing tag (other than itself) is rejected with a bad request. The
     * color is optional - if omitted (null/blank), the tag's existing color
     * is left untouched; if supplied, it replaces the existing color.
     */
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

    /**
     * Changes a tag's dominant/home topic. Purely organizational - it does not
     * restrict, and has no effect on, which problems/questions the tag is (or
     * can be) attached to.
     */
    @PostMapping( "/{tagId}/Topic/{newTopicId}" )
    @Transactional
    public ResponseEntity<AR<String>> changeTopic(
            @PathVariable Integer tagId, @PathVariable Integer newTopicId ) {
        try {
            Tag   tag   = tagRepo.findById( tagId ).get() ;
            Topic topic = topicRepo.findById( newTopicId ).get() ;
            tag.setTopic( topic ) ;
            tagRepo.save( tag ) ;
            return success() ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    /**
     * Deletes a tag. This cascades at the database level (ON DELETE CASCADE)
     * to silently remove every tag_problem_map/tag_question_map row that
     * referenced it, as well as its tag_recent_usage cache entry - there is no
     * confirmation or reassignment step here. If associations should be moved
     * to another tag first, call mergeTags before deleting.
     */
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

    /**
     * Reassigns every problem/question association from sourceTagId to
     * targetTagId (skipping - not duplicating - any item that already carries
     * both tags), then deletes sourceTagId. Use this ahead of a delete when
     * associations need to be preserved under a different tag rather than
     * dropped.
     */
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

    private List<TagVO> toVOs( Iterable<Tag> tags ) {
        List<TagVO> vos = new ArrayList<>() ;
        tags.forEach( t -> vos.add( new TagVO( t ) ) ) ;
        return vos ;
    }

    private List<TagVO> toOrderedVOs( List<Integer> orderedIds ) {
        Map<Integer, Tag> tagsById = new HashMap<>() ;
        tagRepo.findAllById( orderedIds ).forEach( t -> tagsById.put( t.getId(), t ) ) ;

        List<TagVO> vos = new ArrayList<>() ;
        for( Integer id : orderedIds ) {
            Tag tag = tagsById.get( id ) ;
            if( tag != null ) {
                vos.add( new TagVO( tag ) ) ;
            }
        }
        return vos ;
    }
}
