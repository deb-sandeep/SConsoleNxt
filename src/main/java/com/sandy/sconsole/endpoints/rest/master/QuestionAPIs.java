package com.sandy.sconsole.endpoints.rest.master;

import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.core.util.StringUtil;
import com.sandy.sconsole.dao.test.repo.QuestionRepo;
import com.sandy.sconsole.endpoints.rest.master.vo.QuestionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.sandy.sconsole.core.api.AR.success;
import static com.sandy.sconsole.core.api.AR.systemError;

@Slf4j
@RestController
@RequestMapping( "/Master/Question" )
public class QuestionAPIs {
    
    @Autowired
    private QuestionRepo questionRepo = null ;
    
    @PostMapping( "/" )
    @Transactional
    public ResponseEntity<AR<Integer>> saveQuestion( @RequestBody QuestionVO question ) {
        
        log.debug( "QuestionVO: {}", StringUtil.toJSON( question ) ) ;
        try {
            return success(0) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
}
