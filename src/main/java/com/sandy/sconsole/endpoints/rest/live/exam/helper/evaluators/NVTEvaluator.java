package com.sandy.sconsole.endpoints.rest.live.exam.helper.evaluators;

import com.sandy.sconsole.dao.exam.ExamQuestion;
import com.sandy.sconsole.dao.exam.ExamQuestionAttempt;
import com.sandy.sconsole.dao.exam.ExamSection;
import com.sandy.sconsole.endpoints.rest.live.exam.helper.SectionEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope( "prototype" )
public class NVTEvaluator extends SectionEvaluator {
    
    private static final Float TOLERANCE = 0.01F ;
    
    /**
     * This method is called only if the question questionAttempt is either ANSWERED
     * or ANS_AND_MARKED_FOR_REVIEW, so a provided answer is guaranteed to
     * be present.
     */
    @Override
    protected int evaluateQuestionAttempt( ExamSection section,
                                           ExamQuestion question,
                                           ExamQuestionAttempt questionAttempt ) {
        
        int correctMarks = section.getCorrectMarks() ;
        int wrongPenalty = section.getWrongPenalty() ;
        
        String answer = questionAttempt.getAnswerProvided() ;
        String correctAnswer = question.getQuestion().getAnswer() ;
        
        if( answer != null && 
            !answer.isEmpty() ) {
            
            Float refAnswer = Float.parseFloat( correctAnswer ) ;
            Float submittedAnswer = Float.parseFloat( answer ) ;
            
            if( Math.abs( refAnswer - submittedAnswer ) <= TOLERANCE ) {
                questionAttempt.setEvaluationStatus( "CORRECT" ) ;
                return correctMarks ;
            }
            else {
                questionAttempt.setEvaluationStatus( "INCORRECT" ) ;
                return wrongPenalty ;
            }
        }
        else {
            questionAttempt.setEvaluationStatus( "INCORRECT" ) ;
            return wrongPenalty ;
        }
    }
}
