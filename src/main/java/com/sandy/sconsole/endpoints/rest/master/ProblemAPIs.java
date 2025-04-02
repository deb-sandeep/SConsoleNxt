package com.sandy.sconsole.endpoints.rest.master;

import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.master.Problem;
import com.sandy.sconsole.dao.master.TopicProblem;
import com.sandy.sconsole.dao.master.repo.ProblemRepo;
import com.sandy.sconsole.dao.master.repo.TopicProblemRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.sandy.sconsole.core.api.AR.success;
import static com.sandy.sconsole.core.api.AR.systemError;

@Slf4j
@RestController
@RequestMapping( "/Master/Problem" )
public class ProblemAPIs {
    
    @Autowired
    private ProblemRepo problemRepo = null ;
    
    @Autowired
    private TopicProblemRepo tpRepo ;
    
    @PostMapping( "/{problemId}/DifficultyLevel/{difficultyLevel}" )
    public ResponseEntity<AR<String>> updateDifficultyLevel(
            @PathVariable( "problemId" ) int problemId,
            @PathVariable( "difficultyLevel" ) int difficultyLevel ) {
        
        try {
            Problem problem = problemRepo.findById( problemId ).get() ;
            problem.setDifficultyLevel( difficultyLevel ) ;
            problemRepo.save( problem ) ;
            
            return success() ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @GetMapping( "/Pigeons" )
    public ResponseEntity<AR<List<TopicProblem>>> getAllPigeonedProblems() {
        try {
            return success( tpRepo.findAllPigeonedProblems() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
}
