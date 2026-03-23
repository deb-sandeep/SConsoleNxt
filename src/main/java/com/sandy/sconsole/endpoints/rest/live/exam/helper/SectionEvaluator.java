package com.sandy.sconsole.endpoints.rest.live.exam.helper;

import com.sandy.sconsole.dao.exam.ExamQuestion;
import com.sandy.sconsole.dao.exam.ExamQuestionAttempt;
import com.sandy.sconsole.dao.exam.ExamQuestionAttemptRepo;
import com.sandy.sconsole.dao.exam.ExamSection;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

public abstract class SectionEvaluator {
    
    @Autowired
    protected ExamQuestionAttemptRepo eqaRepo ;
    
    public final int evaluateSectionAttempt( ExamSection section,
                                             List<ExamQuestionAttempt> questionAttempts ) {
        
        int totalScore = 0 ;
        for( ExamQuestion question : section.getQuestions() ) {
            ExamQuestionAttempt questionAttempt = findQuestionAttempt( question, questionAttempts ) ;
            totalScore += evaluateQuestionAttempt( section, question, questionAttempt ) ;
            
            eqaRepo.save( questionAttempt ) ;
        }
        return totalScore ;
    }
    
    private ExamQuestionAttempt findQuestionAttempt( ExamQuestion question,
                                                     List<ExamQuestionAttempt> attempts ) {
        
        for( ExamQuestionAttempt attempt : attempts ) {
            if( Objects.equals( attempt.getExamQuestion().getId(),
                                question.getId() ) ) {
                return attempt ;
            }
        }
        throw new IllegalStateException( "Question attempt not found for examQuestion " +
                                         question.getId() ) ;
    }
    
    /**
     * It is expected that the concrete implementation populates the evaluationStatus
     * and score in the questionAttempt instance. The persistence will be
     * taken care of by the superclass logic and hence the implementation should
     * not try to persist the entity.
     *
     * @return The evaluation score for the question attempt.
     */
    protected abstract int evaluateQuestionAttempt( ExamSection section,
                                                    ExamQuestion question,
                                                    ExamQuestionAttempt questionAttempt ) ;
}
