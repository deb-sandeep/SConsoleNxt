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
    private ExamRepo examRepo ;
    
    @Autowired
    private ExamAttemptRepo examAttemptRepo ;
    
    @Autowired
    private ExamSectionAttemptRepo sectionAttemptRepo ;
    
    @Autowired
    private ExamQuestionAttemptRepo questionAttemptRepo = null ;
    
    @Autowired
    private ExamEventLogRepo eventLogRepo ;
    
    @Autowired
    private RootCauseRepo rootCauseRepo = null ;
    
    public ExamAttemptVO getExamAttempt( int examAttemptId ) {
        ExamAttempt attempt = examAttemptRepo.findById( examAttemptId ).get() ;
        return new ExamAttemptVO( attempt, getExamEvents( examAttemptId ) ) ;
    }
    
    private List<ExamEventVO> getExamEvents( int examAttemptId ) {
        List<ExamEventLog> events = eventLogRepo.findByExamAttemptIdOrderBySequenceAsc( examAttemptId ) ;
        List<ExamEventVO> eventVOList = new ArrayList<>() ;
        
        for( ExamEventLog event : events ) {
            eventVOList.add( new ExamEventVO( event ) ) ;
        }
        return eventVOList ;
    }
    
    /**
     * This method is called when an exam attempt is submitted for evaluation.
     *
     * This method populates the score and loss attributes in the
     * ExamAttempt, ExamSectionAttempt, and ExamQuestionAttempt entities.
     *
     * Each section in the exam is associated with a specific evaluator which
     * is decided based on exam-type and section.
     *
     * The section evaluator computes the score and loss for each question
     * in that section and returns the total score for the section. Based
     * on this, the loss for the section is computed. The section score
     * is then aggregated to compute the total score for the exam attempt.
     */
    @Transactional
    public ExamAttemptVO evaluateExamAttempt( int examAttemptId ) {
        
        ExamAttempt attempt = examAttemptRepo.findById( examAttemptId ).get() ;
        Exam exam = attempt.getExam() ;
        
        Set<ExamSection> sections = exam.getSections() ;
        Set<ExamSectionAttempt> sectionAttempts = attempt.getSectionAttempts() ;
        
        int totalExamScore = 0 ;
        
        for( ExamSection section : sections ) {
            ExamSectionAttempt sectionAttempt = findSectionAttempt( section, sectionAttempts ) ;
            if( sectionAttempt != null ) {
                // The section evaluation also populates the score and loss
                // in the section attempt entity.
                totalExamScore += evaluateExamSection( section, sectionAttempt ) ;
            }
            else {
                throw new IllegalStateException( "Section attempt not found for section " +
                                                 section.getId() );
            }
        }
        
        // Change the state of the exam so that it is no longer available for tests
        exam.setState( "ATTEMPTED" ) ;
        examRepo.save( exam ) ;

        // Set the score and loss for the exam attempt.
        attempt.setScore( totalExamScore ) ;
        attempt.setLoss( exam.getTotalMarks() - totalExamScore ) ;
        attempt.setStatus( "COMPLETED" ) ;
        
        ExamAttempt savedAttempt = examAttemptRepo.save( attempt ) ;
        
        return new ExamAttemptVO( savedAttempt, getExamEvents( examAttemptId ) ) ;
    }
    
    private ExamSectionAttempt findSectionAttempt(
            ExamSection section,
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
        final int totalSectionMarks = section.getCorrectMarks() * section.getNumCompulsoryQuestions() ;
        
        if( evaluator != null ) {
            Set<ExamQuestionAttempt> qAttempts = sectionAttempt.getQuestionAttempts() ;
            int sectionScore = evaluator.evaluateSectionAttempt( section, qAttempts ) ;
            int sectionLoss = totalSectionMarks - sectionScore ;
            
            sectionAttempt.setScore( sectionScore ) ;
            sectionAttempt.setLoss( sectionLoss ) ;
            sectionAttempt.setAvoidableLoss( sectionLoss ) ;
            sectionAttempt.setAvoidableLossPct( 0F ) ;
            
            if( sectionLoss > 0 ) {
                sectionAttempt.setAvoidableLossPct( 100F ) ;
            }
            
            return sectionScore ;
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
    
    /**
     * This method recomputes the avoidable loss for a question attempt based
     * on the root cause of the loss provided. A root cause cannot be set if
     * the answer is marked as correct or if the evaluation is skipped due
     * to this question being a non-compulsory question.
     *
     * Once the root cause is set, the avoidable loss is recomputed recursively
     * for the exam attempt.
     */
    @Transactional
    public ExamAttemptVO updateQuestionAttemptRootCause( Integer qAttemptId, String rootCause ) {
        
        ExamQuestionAttempt eqa = questionAttemptRepo.findById( qAttemptId ).get() ;
        RootCause rc  = rootCauseRepo.findById( rootCause ).get() ;
        
        if( eqa.getEvaluationStatus().equals( "CORRECT" ) ||
            eqa.getEvaluationStatus().equals( "EVALUATION_SKIPPED" ) ) {
            throw new IllegalStateException( "Cannot set root cause for a correct answer " +
               "or an answer whose evaluation is skipped." ) ;
        }
        else {
            eqa.setRootCause( rc ) ;
            questionAttemptRepo.save( eqa ) ;
            
            return computeAvoidableLossForExam( eqa.getExamSectionAttempt().getExamAttempt() ) ;
        }
    }
    
    /**
     * This method is called after the root cause for a question attempt is set.
     * Based on the avoidable or unavoidable nature of the root cause, the
     * avoidable loss is computed/recomputed. The avoidable loss of the parent
     * section attempt and exam attempt are recomputed.
     *
     * Note that this method computes only the avoidable loss and its percentage.
     * The loss is assumed to be precomputed during either evaluation or
     * marks override.
     *
     * Only questions attempts which have been evaluated as INCORRECT, PARTIAL,
     * and UNANSWERED are considered for avoidable loss computation.
     */
    private ExamAttemptVO computeAvoidableLossForExam( ExamAttempt examAttempt ) {
        
        int totalLoss = examAttempt.getExam().getTotalMarks() - examAttempt.getScore() ;
        int totalAvoidableLoss = 0 ;
        
        // Set these quantities to zero. They will be freshly populated at the
        // end of this method.
        examAttempt.setAvoidableLoss( 0 ) ;
        examAttempt.setAvoidableLossPct( 0F ) ;
        
        for( ExamSectionAttempt sectionAttempt : examAttempt.getSectionAttempts() ) {
            
            sectionAttempt.setAvoidableLoss( 0 ) ;
            sectionAttempt.setAvoidableLossPct( 0F ) ;

            int sectionTotalLoss =  sectionAttempt.getLoss() ;
            
            if( sectionTotalLoss != 0 ) {
                int sectionAvoidableLoss = computeSectionAvoidableLoss( sectionAttempt ) ;
                float sectionAvoidableLossPct = ( sectionAvoidableLoss * 100F ) / sectionTotalLoss ;
                
                sectionAttempt.setAvoidableLoss( sectionAvoidableLoss ) ;
                sectionAttempt.setAvoidableLossPct( sectionAvoidableLossPct ) ;
                
                totalAvoidableLoss += sectionAvoidableLoss ;
            }
            sectionAttemptRepo.save( sectionAttempt ) ;
        }

        if( totalLoss != 0 ) {
            float avoidableLossPct = ( totalAvoidableLoss * 100F ) / totalLoss ;
            
            examAttempt.setAvoidableLoss( totalAvoidableLoss ) ;
            examAttempt.setAvoidableLossPct( avoidableLossPct ) ;
        }
        
        ExamAttempt savedAttempt = examAttemptRepo.save( examAttempt ) ;
        return new ExamAttemptVO( savedAttempt, null ) ;
    }

    private int computeSectionAvoidableLoss( ExamSectionAttempt sectionAttempt ) {
        
        int secAvoidableLoss = 0 ;
        
        for( ExamQuestionAttempt qAttempt : sectionAttempt.getQuestionAttempts() ) {
            
            // Only if the evaluation status is not correct or the evaluation is
            // not skipped, there is a loss that needs to be checked for avoidable or not.
            String evaluationStatus = qAttempt.getEvaluationStatus() ;
            if( "INCORRECT".equals( evaluationStatus ) ||
                "PARTIAL".equals( evaluationStatus ) ||
                "UNANSWERED".equals( evaluationStatus ) ) {
                
                RootCause rootCause = qAttempt.getRootCause() ;
                boolean isAvoidableLoss = rootCause == null ||
                                          "AVOIDABLE".equals( rootCause.getGroup() ) ;
                
                int avoidableLoss = 0 ;
                if( isAvoidableLoss ) {
                    avoidableLoss = qAttempt.getLoss() ;
                }
                else {
                    // If the loss was unavoidable, and we have got negative marks,
                    // then the negative marks could have been avoided.
                    if( qAttempt.getScore() < 0 ) {
                        avoidableLoss = -qAttempt.getScore() ;
                    }
                }
                
                qAttempt.setAvoidableLoss( avoidableLoss ) ;
                secAvoidableLoss += avoidableLoss ;
            }
            else {
                qAttempt.setAvoidableLoss( 0 ) ;
            }
            
            questionAttemptRepo.save( qAttempt ) ;
        }
        return secAvoidableLoss ;
    }
    
    /**
     * This method is called in rare cases when the exam has set an answer wrong
     * or the examiner decides that the answer provided by the student is valid
     * in the context of assumptions taken. In these cases, the examiner can
     * specifically override the marks from anywhere between the min mark possible
     * to the max mark possible.
     *
     * If such a situation happens, the following logic is applied:
     *
     * - If the overridden score is:
     *   - equal to full marks, then
     *      - any assigned root cause is removed
     *      - the evaluation status is set to CORRECT
     *   - equal to minium marks
     *      - any assigned root cause is left as is
     *      - the evaluation status is set to INCORRECT
     *   - else if the score is somewhere in between
     *      - any assigned root cause is left as is
     *      - the evaluation status is set to PARTIAL
     *
     * - The score of the exam is recomputed recursively.
     * - The avoidable loss of the exam is recomputed recursively.
     */
    @Transactional
    public ExamAttemptVO overrideScore( Integer qAttemptId, int score ) {
        
        ExamQuestionAttempt eqa = questionAttemptRepo.findById( qAttemptId ).get() ;
        ExamSectionAttempt sectionAttempt = eqa.getExamSectionAttempt() ;
        ExamAttempt examAttempt = sectionAttempt.getExamAttempt() ;
        ExamSection section = sectionAttempt.getExamSection() ;
        
        if( score > section.getCorrectMarks() ||
            score < section.getWrongPenalty() ) {
            throw new IllegalArgumentException( "Overridden score can't be " +
                    "more than max score or less than min score." ) ;
        }
        else if( "EVALUATION_SKIPPED".equals( eqa.getEvaluationStatus() ) ) {
            throw new IllegalArgumentException( "Score can't be overridden " +
                    "for a question whose evaluation is skipped." ) ;
        }
        
        eqa.setScore( score ) ;
        eqa.setLoss( section.getCorrectMarks() - score ) ;
        
        if( score == section.getCorrectMarks() ) {
            eqa.setEvaluationStatus( "CORRECT" ) ;
            eqa.setRootCause( null ) ;
        }
        else if( score == section.getWrongPenalty() ) {
            eqa.setEvaluationStatus( "INCORRECT" ) ;
        }
        else {
            eqa.setEvaluationStatus( "PARTIAL" ) ;
        }
        questionAttemptRepo.save( eqa ) ;
        
        recomputeExamScoreAndLoss( examAttempt ) ;
        return computeAvoidableLossForExam( examAttempt ) ;
    }
    
    private void recomputeExamScoreAndLoss( ExamAttempt examAttempt ) {
        
        int totalExamScore = 0 ;
        
        for( ExamSectionAttempt sectionAttempt : examAttempt.getSectionAttempts() ) {
            
            ExamSection section = sectionAttempt.getExamSection() ;
            
            final int correctMarks = section.getCorrectMarks() ;
            final int totalSectionMarks = correctMarks * section.getNumCompulsoryQuestions() ;
            
            int totalSectionScore = 0 ;

            for( ExamQuestionAttempt qAttempt : sectionAttempt.getQuestionAttempts() ) {
                totalSectionScore += qAttempt.getScore() ;
            }
            
            sectionAttempt.setScore( totalSectionScore ) ;
            sectionAttempt.setLoss( totalSectionMarks - totalSectionScore ) ;
            
            sectionAttemptRepo.save( sectionAttempt ) ;
            totalExamScore += totalSectionScore ;
        }

        examAttempt.setScore( totalExamScore ) ;
        examAttempt.setLoss( examAttempt.getExam().getTotalMarks() - totalExamScore ) ;
        
        examAttemptRepo.save( examAttempt ) ;
    }
}
