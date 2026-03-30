package com.sandy.sconsole.endpoints.rest.live.exam.helper;

import com.sandy.sconsole.dao.exam.*;
import com.sandy.sconsole.dao.exam.repo.ExamAttemptRepo;
import com.sandy.sconsole.dao.exam.repo.ExamRepo;
import com.sandy.sconsole.dao.exam.repo.ExamSectionAttemptRepo;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.reqres.CreateExamAttemptRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Scope( "prototype" )
public class ExamAttemptHelper {
    
    @Autowired
    private ExamRepo examRepo ;

    @Autowired
    private ExamAttemptRepo examAttemptRepo ;
    
    @Autowired
    private ExamSectionAttemptRepo examSectionAttemptRepo ;
    
    @Autowired
    private ExamQuestionAttemptRepo examQuestionAttemptRepo ;
    
    @Transactional
    public CreateExamAttemptRes createExamAttempt( int examId ) {
        Exam exam = examRepo.findById( examId ).get() ;
        Map<Integer, Integer> questionAttemptIds = new HashMap<>() ;
        
        ExamAttempt examAttempt = createExamAttempt( exam ) ;
        
        for( ExamSection section : exam.getSections() ) {
            ExamSectionAttempt esAttempt = createExamSectionAttempt( examAttempt, section ) ;
            examAttempt.getSectionAttempts().add( esAttempt ) ;
            
            for( ExamQuestion question : section.getQuestions() ) {
                ExamQuestionAttempt questionAttempt = createExamQuestionAttempt( question, esAttempt ) ;
                esAttempt.getQuestionAttempts().add( questionAttempt ) ;
                
                questionAttemptIds.put( question.getId(), questionAttempt.getId() ) ;
            }
        }
        
        exam.setState( "IN_PROGRESS" ) ;
        examRepo.saveAndFlush( exam ) ;
        
        return new CreateExamAttemptRes( examId, examAttempt.getId(), questionAttemptIds ) ;
    }
    
    private ExamAttempt createExamAttempt( Exam exam ) {
        
        ExamAttempt examAttempt = new ExamAttempt() ;
        examAttempt.setExam( exam ) ;
        examAttempt.setAttemptDate( new Date().toInstant() ) ;
        examAttempt.setScore( 0 ) ;
        examAttempt.setAvoidableLossPct( 0F ) ;
        examAttempt.setUnavoidableLossPct( 0F ) ;
        examAttempt.setStatus( "IN_PROGRESS" ) ;
        return examAttemptRepo.saveAndFlush( examAttempt ) ;
    }
    
    private ExamSectionAttempt createExamSectionAttempt(
            ExamAttempt examAttempt, ExamSection section ) {
    
        ExamSectionAttempt esAttempt = new ExamSectionAttempt() ;
        esAttempt.setExamSection( section ) ;
        esAttempt.setExamAttempt( examAttempt ) ;
        esAttempt.setScore( 0 ) ;
        esAttempt.setAvoidableLossPct( 0F ) ;
        esAttempt.setUnavoidableLossPct( 0F ) ;
        return examSectionAttemptRepo.saveAndFlush( esAttempt ) ;
    }
    
    private ExamQuestionAttempt createExamQuestionAttempt(
            ExamQuestion question, ExamSectionAttempt esAttempt ) {

        ExamQuestionAttempt questionAttempt = new ExamQuestionAttempt() ;
        questionAttempt.setExamQuestion( question ) ;
        questionAttempt.setExamSectionAttempt( esAttempt ) ;
        questionAttempt.setTimeSpent( 0 ) ;
        questionAttempt.setEvaluationStatus( "UNANSWERED" ) ;
        questionAttempt.setAnswerProvided( null ) ;
        questionAttempt.setAnswerSubmitStatus( "NOT_VISITED" ) ;
        questionAttempt.setRootCause( null ) ;
        questionAttempt.setScore( 0 ) ;
        return examQuestionAttemptRepo.saveAndFlush( questionAttempt ) ;
    }
}
