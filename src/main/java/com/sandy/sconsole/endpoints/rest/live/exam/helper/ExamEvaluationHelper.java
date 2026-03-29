package com.sandy.sconsole.endpoints.rest.live.exam.helper;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.dao.exam.*;
import com.sandy.sconsole.dao.exam.repo.ExamAttemptRepo;
import com.sandy.sconsole.dao.exam.repo.ExamEventLogRepo;
import com.sandy.sconsole.dao.exam.repo.ExamSectionAttemptRepo;
import com.sandy.sconsole.dao.exam.repo.RootCauseRepo;
import com.sandy.sconsole.endpoints.rest.live.exam.helper.evaluators.SCAEvaluator;
import com.sandy.sconsole.endpoints.rest.live.exam.vo.ExamAttemptVO;
import com.sandy.sconsole.endpoints.rest.live.exam.vo.ExamEventVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Component
@Scope( "prototype" )
public class ExamEvaluationHelper {
    
    @Autowired
    private ExamAttemptRepo examAttemptRepo ;
    
    @Autowired
    private ExamSectionAttemptRepo sectionAttemptRepo ;
    
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
    
    @Transactional
    public void updateQuestionAttemptRootCause( Integer qAttemptId, String rootCause ) {
        ExamQuestionAttempt eqa = eqaRepo.findById( qAttemptId ).get() ;
        RootCause rc  = rootCauseRepo.findById( rootCause ).get() ;
        eqa.setRootCause( rc ) ;
        eqaRepo.save( eqa ) ;
        
        recomputeLossAttributionPct( eqa ) ;
    }
    
    private void recomputeLossAttributionPct( ExamQuestionAttempt eqa ) {
        
        ExamAttempt examAttempt = eqa.getExamSectionAttempt().getExamAttempt() ;
        int totalLostMarks = examAttempt.getExam().getTotalMarks() - examAttempt.getScore() ;
        int totalAvoidableLossMarks = 0 ;
        float avoidableLossPct ;
        
        examAttempt.setAvoidableLossPct( 0F ) ;
        examAttempt.setUnavoidableLossPct( 0F ) ;
        
        for( ExamSectionAttempt sectionAttempt : examAttempt.getSectionAttempts() ) {
            
            sectionAttempt.setAvoidableLossPct( 0F ) ;
            sectionAttempt.setUnavoidableLossPct( 0F ) ;

            int sectionLostMarks = computeSectionLostMarks( sectionAttempt ) ;
            float sectionAvoidableLossPct ;
            
            if( sectionLostMarks != 0 ) {
                int sectionAvoidableLossMarks = computeSectionAvoidableLossMarks( sectionAttempt ) ;
                
                totalAvoidableLossMarks += sectionAvoidableLossMarks ;
                sectionAvoidableLossPct = ( sectionAvoidableLossMarks * 100F ) / sectionLostMarks ;

                sectionAttempt.setAvoidableLossPct( sectionAvoidableLossPct ) ;
                sectionAttempt.setUnavoidableLossPct( 100F - sectionAvoidableLossPct ) ;
            }
        
            sectionAttemptRepo.save( sectionAttempt ) ;
        }

        if( totalLostMarks != 0 ) {
            avoidableLossPct = ( totalAvoidableLossMarks * 100F ) / totalLostMarks ;
            examAttempt.setAvoidableLossPct( avoidableLossPct ) ;
            examAttempt.setUnavoidableLossPct( 100F - avoidableLossPct ) ;
        }
        
        examAttemptRepo.save( examAttempt ) ;
    }

    private int computeSectionAvoidableLossMarks( ExamSectionAttempt sectionAttempt ) {
        int avoidableLoss = 0 ;
        for( ExamQuestionAttempt qAttempt : sectionAttempt.getQuestionAttempts() ) {
            if( !hasAttributableLoss( qAttempt ) ) continue ;
            
            RootCause rootCause = qAttempt.getRootCause() ;
            boolean isAvoidableLoss = rootCause == null ||
                                      !"UNAVOIDABLE".equals( rootCause.getGroup() ) ;
            
            if( isAvoidableLoss ) {
                avoidableLoss += sectionAttempt.getExamSection().getCorrectMarks() -
                                 qAttempt.getScore() ;
            }
        }
        return avoidableLoss ;
    }

    private boolean hasAttributableLoss( ExamQuestionAttempt qAttempt ) {
        String evalStatus = qAttempt.getEvaluationStatus() ;
        return "INCORRECT".equals( evalStatus ) ||
               "PARTIAL".equals( evalStatus ) ||
               "UNANSWERED".equals( evalStatus ) ;
    }

    private int computeSectionLostMarks( ExamSectionAttempt sectionAttempt ) {
        ExamSection section = sectionAttempt.getExamSection() ;
        int sectionTotalMarks = section.getCorrectMarks() * section.getNumCompulsoryQuestions() ;
        return sectionTotalMarks - sectionAttempt.getScore() ;
    }

    @Transactional
    public void overrideScore( Integer qAttemptId, int score ) {
        
        ExamQuestionAttempt eqa = eqaRepo.findById( qAttemptId ).get() ;
        ExamSectionAttempt sectionAttempt = eqa.getExamSectionAttempt() ;
        ExamAttempt examAttempt = sectionAttempt.getExamAttempt() ;
        
        eqa.setScore( score ) ;
        if( score == sectionAttempt.getExamSection().getCorrectMarks() ) {
            eqa.setEvaluationStatus( "CORRECT" ) ;
            eqa.setRootCause( null ) ;
        }
        else if( score == sectionAttempt.getExamSection().getWrongPenalty() ) {
            eqa.setEvaluationStatus( "INCORRECT" ) ;
        }
        else {
            eqa.setEvaluationStatus( "PARTIAL" ) ;
        }
        eqaRepo.save( eqa ) ;
        
        recomputeScore( examAttempt ) ;
        recomputeLossAttributionPct( eqa ) ;
    }
    
    private void recomputeScore( ExamAttempt examAttempt ) {
        int totalScore = 0 ;
        for( ExamSectionAttempt sectionAttempt : examAttempt.getSectionAttempts() ) {
            int sectionScore = 0 ;

            for( ExamQuestionAttempt qAttempt : sectionAttempt.getQuestionAttempts() ) {
                sectionScore += qAttempt.getScore() ;
            }
            
            sectionAttempt.setScore( sectionScore ) ;
            sectionAttemptRepo.save( sectionAttempt ) ;
            totalScore += sectionScore ;
        }

        examAttempt.setScore( totalScore ) ;
        examAttemptRepo.save( examAttempt ) ;
    }
}
