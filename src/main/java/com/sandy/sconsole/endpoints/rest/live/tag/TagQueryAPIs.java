package com.sandy.sconsole.endpoints.rest.live.tag;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.endpoints.rest.live.tag.helper.TagQueryHelper;
import com.sandy.sconsole.endpoints.rest.live.tag.vo.SavedTagQueryVO;
import com.sandy.sconsole.endpoints.rest.live.tag.vo.reqres.SaveQueryReq;
import com.sandy.sconsole.endpoints.rest.live.tag.vo.reqres.TagQuerySearchReq;
import com.sandy.sconsole.endpoints.rest.live.tag.vo.reqres.TagQuerySearchRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.sandy.sconsole.core.api.AR.*;

/**
 * Searches Problems and Questions by an arbitrary boolean AND/OR/NOT
 * expression over tags, plus simple filters, returning every matching row of
 * each in one response (no paging). Also lets a user save/recall/delete
 * search criteria for reuse. The route stays under /TagQuery (not /Master)
 * since this queries live tag-association data rather than master/reference
 * data.
 */
@Slf4j
@RestController
@RequestMapping( "/TagQuery" )
public class TagQueryAPIs {

    @PostMapping( "/Search" )
    public ResponseEntity<AR<TagQuerySearchRes>> search( @RequestBody TagQuerySearchReq req ) {
        try {
            TagQueryHelper helper = SConsole.getBean( TagQueryHelper.class ) ;
            return success( helper.search( req ) ) ;
        }
        catch( IllegalArgumentException e ) {
            return badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    /**
     * Saves a named search (the full tagQuery + filters criteria) for later
     * recall.
     */
    @PostMapping( "/SaveQuery" )
    @Transactional
    public ResponseEntity<AR<SavedTagQueryVO>> saveQuery( @RequestBody SaveQueryReq req ) {
        try {
            TagQueryHelper helper = SConsole.getBean( TagQueryHelper.class ) ;
            return success( helper.saveQuery( req ) ) ;
        }
        catch( IllegalArgumentException e ) {
            return badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    /**
     * Deletes a previously saved query.
     */
    @DeleteMapping( "/SavedQuery/{id}" )
    @Transactional
    public ResponseEntity<AR<String>> deleteQuery( @PathVariable Integer id ) {
        try {
            TagQueryHelper helper = SConsole.getBean( TagQueryHelper.class ) ;
            helper.deleteQuery( id ) ;
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
     * Lists every saved query (id + name) for a picker UI.
     */
    @GetMapping( "/SavedQueries" )
    public ResponseEntity<AR<List<SavedTagQueryVO>>> getSavedQueries() {
        try {
            TagQueryHelper helper = SConsole.getBean( TagQueryHelper.class ) ;
            return success( helper.getSavedQueries() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    /**
     * Returns the tagQuery+filters criteria of a saved query, ready to feed
     * straight back into /Search.
     */
    @GetMapping( "/SavedQuery/{id}" )
    public ResponseEntity<AR<TagQuerySearchReq>> getQuery( @PathVariable Integer id ) {
        try {
            TagQueryHelper helper = SConsole.getBean( TagQueryHelper.class ) ;
            return success( helper.getQuery( id ) ) ;
        }
        catch( IllegalArgumentException e ) {
            return badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
}
