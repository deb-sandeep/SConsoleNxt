package com.sandy.sconsole.endpoints.rest.master.exam;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.exam.ExamQuestionAttemptRepo;
import com.sandy.sconsole.dao.exam.RootCause;
import com.sandy.sconsole.dao.exam.repo.ExamRepo;
import com.sandy.sconsole.dao.exam.repo.RootCauseRepo;
import com.sandy.sconsole.endpoints.rest.master.exam.helper.ExamHelper;
import com.sandy.sconsole.endpoints.rest.master.exam.helper.ExamUpdateHelper;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.ExamVO;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.RootCauseVO;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.reqres.SaveExamRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.sandy.sconsole.core.api.AR.systemError;

@Slf4j
@RestController
@RequestMapping( "/Master/Exam" )
public class ExamAPIs {
    
    @Autowired
    private ExamRepo examRepo = null ;
    
    @Autowired
    private RootCauseRepo rootCauseRepo = null ;
    
    @Autowired
    private ExamQuestionAttemptRepo eqaRepo = null ;
    
    @GetMapping( "/" )
    public ResponseEntity<AR<List<ExamVO>>> getListOfExams() {
        
        try {
            ExamHelper helper = SConsole.getBean( ExamHelper.class ) ;
            List<ExamVO> examList = helper.getListOfExams() ;
            return AR.success( examList ) ;
        }
        catch( IllegalArgumentException e ) {
            return AR.badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @GetMapping( "/RootCauses" )
    public ResponseEntity<AR<List<RootCauseVO>>> getRootCauses() {
        
        try {
            List<RootCause> rootCauses = rootCauseRepo.findAll() ;
            List<RootCauseVO> res = new ArrayList<>() ;
            for( RootCause rc : rootCauses ) {
                res.add( RootCauseVO.from( rc ) ) ;
            }
            res.sort( Comparator.comparing( RootCauseVO::cause ) ) ;
            return AR.success( res ) ;
        }
        catch( IllegalArgumentException e ) {
            return AR.badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @GetMapping( "/{examId}" )
    public ResponseEntity<AR<ExamVO>> getExamConfig( @PathVariable int examId ) {
        
        try {
            ExamHelper helper = SConsole.getBean( ExamHelper.class ) ;
            ExamVO examVO = helper.getExamConfig( examId ) ;
            return AR.success( examVO ) ;
        }
        catch( IllegalArgumentException e ) {
            return AR.badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @PostMapping( "/" )
    public ResponseEntity<AR<SaveExamRes>> createExam( @RequestBody ExamVO exam ) {
        
        try {
            ExamHelper helper = SConsole.getBean( ExamHelper.class ) ;
            int examId = helper.createExam( exam ) ;
            return AR.success( SaveExamRes.success( examId ) ) ;
        }
        catch( IllegalArgumentException e ) {
            return AR.badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @PutMapping( "/" )
    public ResponseEntity<AR<SaveExamRes>> updateExam( @RequestBody ExamVO exam ) {

        try {
            ExamUpdateHelper helper = SConsole.getBean( ExamUpdateHelper.class ) ;
            int examId = helper.updateExam( exam ) ;
            return AR.success( SaveExamRes.success( examId ) ) ;
        }
        catch( IllegalArgumentException e ) {
            return AR.badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
    
    @DeleteMapping( "/{examId}" )
    @Transactional
    public ResponseEntity<AR<String>> deleteExamConfig( @PathVariable int examId ) {
        
        try {
            this.examRepo.deleteById( examId ) ;
            return AR.success() ;
        }
        catch( IllegalArgumentException e ) {
            return AR.badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
}
