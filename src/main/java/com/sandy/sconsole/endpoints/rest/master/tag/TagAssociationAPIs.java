package com.sandy.sconsole.endpoints.rest.master.tag;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.endpoints.rest.master.tag.helper.TagAssociationHelper;
import com.sandy.sconsole.endpoints.rest.master.tag.vo.TagVO;
import com.sandy.sconsole.endpoints.rest.master.tag.vo.reqres.ItemIdsReq;
import com.sandy.sconsole.endpoints.rest.master.tag.vo.reqres.SetTagsReq;
import com.sandy.sconsole.endpoints.rest.master.tag.vo.reqres.TagAssociationRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.sandy.sconsole.core.api.AR.*;

@Slf4j
@RestController
@RequestMapping( "/Master/TagAssociation" )
public class TagAssociationAPIs {

    /**
     * Attaches tagId to every item in req.itemIds (itemType is PROBLEM or
     * QUESTION, applying to all of them). Already-tagged items are silently
     * skipped rather than treated as an error - both via a pre-check before
     * insert, and as a fallback via a silently-ignored DataIntegrityViolationException
     * should a duplicate slip through. An itemId that doesn't resolve to a
     * real problem/question aborts the whole batch with a bad request; no
     * partial application is reported back to the caller.
     */
    @PostMapping( "/{itemType}/{tagId}" )
    @Transactional
    public ResponseEntity<AR<String>> addTag(
            @PathVariable TaggableItemType itemType,
            @PathVariable Integer tagId,
            @RequestBody ItemIdsReq req ) {
        try {
            TagAssociationHelper helper = SConsole.getBean( TagAssociationHelper.class ) ;
            helper.addTag( itemType, req.itemIds(), tagId ) ;
            return success() ;
        }
        catch( DataIntegrityViolationException dive ) {
            log.debug( "Ignoring duplicate tag association.", dive ) ;
            return success() ;
        }
        catch( IllegalArgumentException e ) {
            return badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    /**
     * Detaches a single tag from a single item. A no-op (still returns
     * success) if the item didn't have that tag to begin with.
     */
    @DeleteMapping( "/{itemType}/{itemId}/{tagId}" )
    @Transactional
    public ResponseEntity<AR<String>> removeTag(
            @PathVariable TaggableItemType itemType,
            @PathVariable Integer itemId,
            @PathVariable Integer tagId ) {
        try {
            TagAssociationHelper helper = SConsole.getBean( TagAssociationHelper.class ) ;
            helper.removeTag( itemType, itemId, tagId ) ;
            return success() ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    /**
     * Returns every tag attached to a single item.
     */
    @GetMapping( "/{itemType}/{itemId}" )
    public ResponseEntity<AR<List<TagVO>>> getTagsForItem(
            @PathVariable TaggableItemType itemType,
            @PathVariable Integer itemId ) {
        try {
            TagAssociationHelper helper = SConsole.getBean( TagAssociationHelper.class ) ;
            return success( helper.getTagsForItem( itemType, itemId ) ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    /**
     * Batch version of getTagsForItem: for each id in req.itemIds(), returns
     * every tag attached to that item, keyed by item id. Every requested id
     * is present in the response (empty list if untagged), so callers never
     * need to special-case a missing key.
     */
    @PostMapping( "/{itemType}/Tags" )
    public ResponseEntity<AR<Map<Integer, List<TagVO>>>> getTagsForItems(
            @PathVariable TaggableItemType itemType,
            @RequestBody ItemIdsReq req ) {
        try {
            TagAssociationHelper helper = SConsole.getBean( TagAssociationHelper.class ) ;
            return success( helper.getTagsForItems( itemType, req.itemIds() ) ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    /**
     * Detaches every tag from every item in req.itemIds (itemType applying to
     * all of them). Items with no tags are simply left untouched.
     */
    @DeleteMapping( "/{itemType}" )
    @Transactional
    public ResponseEntity<AR<String>> removeAllTags(
            @PathVariable TaggableItemType itemType,
            @RequestBody ItemIdsReq req ) {
        try {
            TagAssociationHelper helper = SConsole.getBean( TagAssociationHelper.class ) ;
            helper.removeAllTags( itemType, req.itemIds() ) ;
            return success() ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    /**
     * Atomically reconciles a single item's tag set to exactly req.tagIds() -
     * removing whatever isn't in the target list and adding whatever's
     * missing from it - in one transaction, and returns the resulting tag
     * list. Intended for a chip-editor style UI that saves its whole tag set
     * in one call rather than composing many individual add/remove calls.
     */
    @PostMapping( "/{itemType}/{itemId}/Set" )
    @Transactional
    public ResponseEntity<AR<List<TagVO>>> setTags(
            @PathVariable TaggableItemType itemType,
            @PathVariable Integer itemId,
            @RequestBody SetTagsReq req ) {
        try {
            TagAssociationHelper helper = SConsole.getBean( TagAssociationHelper.class ) ;
            return success( helper.setTags( itemType, itemId, req.tagIds() ) ) ;
        }
        catch( IllegalArgumentException e ) {
            return badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    /**
     * Batch tag-count lookup: for each id in req.itemIds(), returns how many
     * tags that item has, keyed by item id. Every requested id is present in
     * the response (0 if untagged), so callers never need to special-case a
     * missing key. Intended for listing pages showing many items at once,
     * where a per-row query would be impractical - one call covers the whole
     * page.
     */
    @PostMapping( "/{itemType}/Counts" )
    public ResponseEntity<AR<Map<Integer, Integer>>> getTagCounts(
            @PathVariable TaggableItemType itemType,
            @RequestBody ItemIdsReq req ) {
        try {
            TagAssociationHelper helper = SConsole.getBean( TagAssociationHelper.class ) ;
            return success( helper.getTagCounts( itemType, req.itemIds() ) ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    /**
     * The inverse of Counts: given a set of item ids, returns every distinct
     * tag attached to at least one of them, with associationCount set to how
     * many of the given items carry that tag (not the tag's total usage
     * across the whole system). Ordered by count descending, tag text as
     * tie-break. Useful for e.g. a tag cloud over a filtered/selected batch
     * of problems or questions.
     */
    @PostMapping( "/{itemType}/Histogram" )
    public ResponseEntity<AR<List<TagVO>>> getTagAssociationHistogram(
            @PathVariable TaggableItemType itemType,
            @RequestBody ItemIdsReq req ) {
        try {
            TagAssociationHelper helper = SConsole.getBean( TagAssociationHelper.class ) ;
            return success( helper.getTagAssociationHistogram( itemType, req.itemIds() ) ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    /**
     * Returns every problem and question tagged with tagId, as two separate
     * lists in the response. The question side uses the lightweight
     * QuestionSummaryVO (no image metadata) rather than the full QuestionVO.
     */
    @GetMapping( "/ForTag/{tagId}" )
    public ResponseEntity<AR<TagAssociationRes>> getItemsForTag( @PathVariable Integer tagId ) {
        try {
            TagAssociationHelper helper = SConsole.getBean( TagAssociationHelper.class ) ;
            return success( helper.getItemsForTag( tagId ) ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
}
