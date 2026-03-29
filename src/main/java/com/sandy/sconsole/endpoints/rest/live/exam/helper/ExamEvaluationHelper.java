package com.sandy.sconsole.endpoints.rest.live.exam.helper;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.dao.exam.*;
import com.sandy.sconsole.dao.exam.repo.*;
import com.sandy.sconsole.endpoints.rest.live.exam.helper.evaluators.SCAEvaluator;
import com.sandy.sconsole.endpoints.rest.live.exam.vo.ExamAttemptVO;
import com.sandy.sconsole.endpoints.rest.live.exam.vo.ExamEventVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Component
@Scope( "prototype" )
public class ExamEvaluationHelper {
    
    @Autowired
    private ExamRepo examRepo ;

    @Autowired
    private ExamAttemptRepo examAttemptRepo ;
    
    @Autowired
    private ExamSectionAttemptRepo sectionAttemptRepo ;
    
    @Autowired
    private ExamQuestionAttemptRepo questionAttemptRepo ;
    
    @Autowired
    private ExamEventLogRepo eventLogRepo ;
    
    @Autowired
    private ExamQuestionAttemptRepo eqaRepo = null ;
    
    @Autowired
    private RootCauseRepo rootCauseRepo = null ;
    
    public ExamAttemptVO evaluateExamAttempt( int examAttemptId ) {
        
        ExamAttempt attempt = examAttemptRepo.findById( examAttemptId ).get() ;
        Exam exam = attempt.getExam() ;
        
        Set<ExamSection> sections = exam.getSections() ;
        Set<ExamSectionAttempt> sectionAttempts = attempt.getSectionAttempts() ;
        
        int totalScore = 0 ;
        
        for( ExamSection section : sections ) {
            ExamSectionAttempt sectionAttempt = findSectionAttempt( section, sectionAttempts ) ;
            if( sectionAttempt != null ) {
                totalScore += evaluateExamSection( section, sectionAttempt ) ;
            }
            else {
                throw new IllegalStateException( "Section attempt not found for section " +
                                                 section.getId() );
            }
        }
        
        attempt.setScore( totalScore ) ;
        attempt.setStatus( "COMPLETED" ) ;
        ExamAttempt savedAttempt = examAttemptRepo.save( attempt ) ;
        
        log.debug( "Exam attempt evaluated: {}", examAttemptId ) ;
        
        return new ExamAttemptVO( savedAttempt, getExamEvents( examAttemptId ) ) ;
    }
    
    public ExamAttemptVO getScaffoldResponse() {
        ExamAttempt attempt = examAttemptRepo.findById( 7 ).get() ;
        return new ExamAttemptVO( attempt, getExamEvents( 7 ) ) ;
    }
    
    private List<ExamEventVO> getExamEvents( int examAttemptId ) {
        List<ExamEventLog> events = eventLogRepo.findByExamAttemptIdOrderBySequenceAsc( examAttemptId ) ;
        List<ExamEventVO> eventVOList = new ArrayList<>() ;
        
        for( ExamEventLog event : events ) {
            eventVOList.add( new ExamEventVO( event ) ) ;
        }
        return eventVOList ;
    }
    
    private ExamSectionAttempt findSectionAttempt( ExamSection section,
                                                   Set<ExamSectionAttempt> sectionAttempts ) {
        for( ExamSectionAttempt sectionAttempt : sectionAttempts ) {
            if( Objects.equals( sectionAttempt.getExamSection().getId(), section.getId() ) ) {
                return sectionAttempt ;
            }
        }
        return null ;
    }

    private int evaluateExamSection( ExamSection section,
                                     ExamSectionAttempt sectionAttempt ) {
        
        String examType = section.getExam().getType() ;
        String problemType = section.getProblemType().getProblemType() ;
        
        SectionEvaluator evaluator = getSectionEvaluator( examType, problemType ) ;
        
        if( evaluator != null ) {
            Set<ExamQuestionAttempt> qAttempts = sectionAttempt.getQuestionAttempts() ;
            int score = evaluator.evaluateSectionAttempt( section, qAttempts ) ;
            
            sectionAttempt.setScore( score ) ;
            return score ;
        }
        else {
            throw new IllegalStateException( "Section " + problemType +
                                             " does not have an evaluator." ) ;
        }
    }
    
    private SectionEvaluator getSectionEvaluator( String examType, String problemType ) {
        if( "SCA".equals( problemType ) ) {
            return SConsole.getBean( SCAEvaluator.class ) ;
        }
        return null ;
    }
    
    public void updateQuestionAttemptRootCause( Integer qAttemptId, String rootCause ) {
        ExamQuestionAttempt eqa = eqaRepo.findById( qAttemptId ).get() ;
        RootCause rc  = rootCauseRepo.findById( rootCause ).get() ;
        eqa.setRootCause( rc ) ;
        eqaRepo.save( eqa ) ;
        
        recomputeLossAttributionPct( eqa ) ;
    }
    
    private void recomputeLossAttributionPct( ExamQuestionAttempt eqa ) {
    
    }
}
