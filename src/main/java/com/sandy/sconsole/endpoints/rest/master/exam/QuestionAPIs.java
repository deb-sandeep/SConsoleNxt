package com.sandy.sconsole.endpoints.rest.master.exam;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.exam.Question;
import com.sandy.sconsole.dao.exam.repo.QuestionRepo;
import com.sandy.sconsole.endpoints.rest.master.exam.helper.QuestionHelper;
import com.sandy.sconsole.endpoints.rest.master.exam.helper.QuestionSearchHelper;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.QuestionVO;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.reqres.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import static com.sandy.sconsole.core.api.AR.systemError;

@Slf4j
@RestController
@RequestMapping( "/Master/Question" )
public class QuestionAPIs {
    
    @Autowired
    private QuestionRepo questionRepo = null ;
    
    @PostMapping( "/" )
    @Transactional
    public ResponseEntity<AR<SaveQuestionRes>> saveQuestion(
            @RequestBody QuestionVO question ) {
        
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
        
        try {
            QuestionSearchHelper helper = SConsole.getBean( QuestionSearchHelper.class ) ;
            AvailableQuestionRes response = helper.getAvailableQuestions( topicId, problemTypes ) ;
            return AR.success( response ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @PostMapping( "/Rating/{questionId}/{rating}" )
    public ResponseEntity<AR<String>> changeRating(
            @PathVariable Integer questionId,
            @PathVariable Integer rating
    ) {
        
        try {
            Question q = questionRepo.findById( questionId ).get() ;
            q.setRating( rating ) ;
            questionRepo.save( q ) ;
            
            return AR.success() ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
}
