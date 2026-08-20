package com.sandy.sconsole.endpoints.rest.live.tag;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.endpoints.rest.live.tag.helper.TagQueryHelper;
import com.sandy.sconsole.endpoints.rest.live.tag.vo.reqres.TagQuerySearchReq;
import com.sandy.sconsole.endpoints.rest.live.tag.vo.reqres.TagQuerySearchRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.sandy.sconsole.core.api.AR.*;

/**
 * Searches Problems and Questions by an arbitrary boolean AND/OR/NOT
 * expression over tags, plus simple filters, returning two independently
 * paginated result sets. The route stays under /Master for compatibility
 * with the already-built frontend contract, even though this queries live
 * tag-association data rather than master/reference data.
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
}
