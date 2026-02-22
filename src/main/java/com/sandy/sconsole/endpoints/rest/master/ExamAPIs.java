package com.sandy.sconsole.endpoints.rest.master;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.core.util.StringUtil;
import com.sandy.sconsole.endpoints.rest.master.helper.ExamHelper;
import com.sandy.sconsole.endpoints.rest.master.vo.ExamVO;
import com.sandy.sconsole.endpoints.rest.master.vo.reqres.SaveExamRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.sandy.sconsole.core.api.AR.systemError;

@Slf4j
@RestController
@RequestMapping( "/Master/Exam" )
public class ExamAPIs {
    
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
