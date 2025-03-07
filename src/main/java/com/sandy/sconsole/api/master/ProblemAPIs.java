package com.sandy.sconsole.api.master;

import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.master.Problem;
import com.sandy.sconsole.dao.master.repo.ProblemRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.sandy.sconsole.core.api.AR.success;
import static com.sandy.sconsole.core.api.AR.systemError;

@Slf4j
@RestController
@RequestMapping( "/Master/Problem" )
public class ProblemAPIs {
    
    @Autowired
    private ProblemRepo problemRepo = null ;
    
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
}
