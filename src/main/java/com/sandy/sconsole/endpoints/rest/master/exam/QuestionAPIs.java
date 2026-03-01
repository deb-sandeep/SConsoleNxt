package com.sandy.sconsole.endpoints.rest.master.exam;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.endpoints.rest.master.exam.helper.QuestionHelper;
import com.sandy.sconsole.endpoints.rest.master.exam.helper.QuestionSearchHelper;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.QuestionVO;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.reqres.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import static com.sandy.sconsole.core.api.AR.systemError;

@Slf4j
@RestController
@RequestMapping( "/Master/Question" )
public class QuestionAPIs {
    
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
    
    @GetMapping( "/RepoStatus" )
    public ResponseEntity<AR<QuestionRepoStatus>> getRepositoryStatus() {
    
        log.debug( "Getting question repository status" ) ;
        try {
            QuestionHelper     helper = SConsole.getBean( QuestionHelper.class ) ;
            QuestionRepoStatus status = helper.getRepositoryStatus() ;
            return AR.success( status ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @PostMapping( "/Search" )
    public ResponseEntity<AR<QuestionSearchRes>> search( @RequestBody QuestionSearchReq req ) {
        
        log.debug( "Searching questions" ) ;
        try {
            QuestionSearchHelper helper = SConsole.getBean( QuestionSearchHelper.class ) ;
            QuestionSearchRes response = helper.search( req ) ;
            return AR.success( response ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @GetMapping( "/AvailableQuestions" )
    public ResponseEntity<AR<AvailableQuestionRes>> getAvailableQuestions(
            @RequestParam( "topicId" ) int topicId,
            @RequestParam( "problemTypes" ) String[] problemTypes ) {
        
        log.debug( "Getting available questions for topic: {} and problem types: {}",
                    topicId, problemTypes ) ;
        try {
            QuestionSearchHelper helper = SConsole.getBean( QuestionSearchHelper.class ) ;
            AvailableQuestionRes response = helper.getAvailableQuestions( topicId, problemTypes ) ;
            return AR.success( response ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
}
