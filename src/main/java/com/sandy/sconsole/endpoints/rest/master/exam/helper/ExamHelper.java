package com.sandy.sconsole.endpoints.rest.master.exam.helper;

import com.sandy.sconsole.dao.exam.Exam;
import com.sandy.sconsole.dao.exam.ExamQuestionAttemptRepo;
import com.sandy.sconsole.dao.exam.repo.ExamAttemptRepo;
import com.sandy.sconsole.dao.exam.repo.ExamRepo;
import com.sandy.sconsole.dao.exam.repo.ExamSectionAttemptRepo;
import com.sandy.sconsole.dao.exam.repo.QuestionRepo;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.ExamVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Scope( "prototype" )
public class ExamHelper {
    
    @Autowired
    private ExamRepo examRepo ;

    @Autowired
    private QuestionRepo questionRepo ;
    
    @Autowired
    private ExamAttemptRepo examAttemptRepo ;
    
    @Autowired
    private ExamSectionAttemptRepo examSectionAttemptRepo ;
    
    @Autowired
    private ExamQuestionAttemptRepo examQuestionAttemptRepo ;
    
    @Transactional
    public int createExam( ExamVO examVO ) {
        Exam exam = new Exam( examVO ) ;
        Exam savedExam = examRepo.saveAndFlush( exam ) ;
        return savedExam.getId() ;
    }
    
    public List<ExamVO> getListOfExams() {
        List<Exam> exams = examRepo.findAll() ;
        List<ExamVO> examVoList = new ArrayList<>() ;
        exams.forEach( e -> examVoList.add( new ExamVO( e, false ) ) ) ;
        return examVoList ;
    }
    
    public ExamVO getExamConfig( int examId ) {
        Exam exam = examRepo.findById( examId ).get() ;
        return new ExamVO( exam, true ) ;
    }
}
