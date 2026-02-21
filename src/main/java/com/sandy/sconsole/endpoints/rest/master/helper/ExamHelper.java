package com.sandy.sconsole.endpoints.rest.master.helper;

import com.sandy.sconsole.endpoints.rest.master.vo.ExamVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope( "prototype" )
public class ExamHelper {
    
    public int saveExam( ExamVO examVO ) throws Exception {
        return 1 ;
    }
}
