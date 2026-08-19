package com.sandy.sconsole.endpoints.rest.master.tag;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.endpoints.rest.master.tag.helper.TagAssociationHelper;
import com.sandy.sconsole.endpoints.rest.master.tag.vo.TagVO;
import com.sandy.sconsole.endpoints.rest.master.tag.vo.reqres.SetTagsReq;
import com.sandy.sconsole.endpoints.rest.master.tag.vo.reqres.TagAssociationRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.sandy.sconsole.core.api.AR.*;

@Slf4j
@RestController
@RequestMapping( "/Master/TagAssociation" )
public class TagAssociationAPIs {

    @PostMapping( "/{itemType}/{itemId}/{tagId}" )
    @Transactional
    public ResponseEntity<AR<String>> addTag(
            @PathVariable TaggableItemType itemType,
            @PathVariable Integer itemId,
            @PathVariable Integer tagId ) {
        try {
            TagAssociationHelper helper = SConsole.getBean( TagAssociationHelper.class ) ;
            helper.addTag( itemType, itemId, tagId ) ;
            return success() ;
        }
        catch( DataIntegrityViolationException dive ) {
            log.error( "Duplicate or invalid tag association.", dive ) ;
            return functionalError( "Tag is already associated with this item.", dive ) ;
        }
        catch( IllegalArgumentException e ) {
            return badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

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

    @DeleteMapping( "/{itemType}/{itemId}" )
    @Transactional
    public ResponseEntity<AR<String>> removeAllTags(
            @PathVariable TaggableItemType itemType,
            @PathVariable Integer itemId ) {
        try {
            TagAssociationHelper helper = SConsole.getBean( TagAssociationHelper.class ) ;
            helper.removeAllTags( itemType, itemId ) ;
            return success() ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

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
