package com.sandy.sconsole.endpoints.rest.master;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.SConsoleConfig;
import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.test.repo.QuestionRepo;
import com.sandy.sconsole.endpoints.rest.master.helper.QuestionHelper;
import com.sandy.sconsole.endpoints.rest.master.vo.QuestionVO;
import com.sandy.sconsole.endpoints.rest.master.vo.reqres.SaveQuestionRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.sandy.sconsole.core.api.AR.systemError;

@Slf4j
@RestController
@RequestMapping( "/Master/Question" )
public class QuestionAPIs {
    
    @Autowired
    private QuestionRepo questionRepo = null ;
    
    @Autowired
    private SConsoleConfig config = null ;
    
    @PostMapping( "/" )
    @Transactional
    public ResponseEntity<AR<SaveQuestionRes>> saveQuestion( @RequestBody QuestionVO question ) {
        
        log.debug( "Saving Question: {}", question.getQuestionId() ) ;
        
        try {
            QuestionHelper helper = SConsole.getBean( QuestionHelper.class ) ;
            int questionId = helper.saveQuestion( question ) ;
            return AR.success( SaveQuestionRes.success( questionId ) ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
}
