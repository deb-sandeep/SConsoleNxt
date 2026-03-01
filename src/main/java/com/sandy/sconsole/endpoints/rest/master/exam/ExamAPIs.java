package com.sandy.sconsole.endpoints.rest.master.exam;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.core.util.StringUtil;
import com.sandy.sconsole.endpoints.rest.master.exam.helper.ExamHelper;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.ExamVO;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.reqres.SaveExamRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.sandy.sconsole.core.api.AR.systemError;

@Slf4j
@RestController
@RequestMapping( "/Master/Exam" )
public class ExamAPIs {
    
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
    
    @GetMapping( "/{examId}" )
    public ResponseEntity<AR<ExamVO>> getExamConfig(
            @PathVariable( "examId" ) int examId
    ) {
        
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
    public ResponseEntity<AR<SaveExamRes>> saveExam( @RequestBody ExamVO exam ) {
        
        log.debug( "Saving Exam: {}", StringUtil.toJSON( exam ) ) ;
        try {
            ExamHelper helper = SConsole.getBean( ExamHelper.class ) ;
            int examId = helper.saveExam( exam ) ;
            return AR.success( SaveExamRes.success( examId ) ) ;
        }
        catch( IllegalArgumentException e ) {
            return AR.badRequest( e.getMessage() ) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
}
