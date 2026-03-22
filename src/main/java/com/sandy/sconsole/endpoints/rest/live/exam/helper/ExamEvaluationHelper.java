package com.sandy.sconsole.endpoints.rest.live.exam.helper;

import com.sandy.sconsole.dao.exam.ExamQuestionAttemptRepo;
import com.sandy.sconsole.dao.exam.repo.ExamAttemptRepo;
import com.sandy.sconsole.dao.exam.repo.ExamRepo;
import com.sandy.sconsole.dao.exam.repo.ExamSectionAttemptRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope( "prototype" )
public class ExamEvaluationHelper {
    
    @Autowired
    private ExamRepo examRepo ;

    @Autowired
    private ExamAttemptRepo examAttemptRepo ;
    
    @Autowired
    private ExamSectionAttemptRepo examSectionAttemptRepo ;
    
    @Autowired
    private ExamQuestionAttemptRepo examQuestionAttemptRepo ;
    
    public void evaluateExam( int examId ) {
        log.debug( "Evaluating exam: {}", examId ) ;
    }
}
