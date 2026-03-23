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
public class SCAEvaluator extends SectionEvaluator {
    
    @Override
    protected int evaluateQuestionAttempt( ExamSection section,
                                           ExamQuestion question,
                                           ExamQuestionAttempt attempt ) {
        
        int correctMarks = section.getCorrectMarks() ;
        int wrongPenalty = section.getWrongPenalty() ;
        String submitStatus = attempt.getAnswerSubmitStatus() ;
        
        if( submitStatus.equals( "ANSWERED" ) ||
            submitStatus.equals( "ANS_AND_MARKED_FOR_REVIEW" ) ) {
        
            String answer = attempt.getAnswerProvided() ;
            String correctAnswer = question.getQuestion().getAnswer() ;
            
            log.debug( "Evaluating question attempt: {}", attempt.getId() ) ;
            log.debug( "Answer: {}, Correct answer: {}", answer, correctAnswer ) ;
            
            if( answer.equalsIgnoreCase( correctAnswer ) ) {
                attempt.setEvaluationStatus( "CORRECT" ) ;
                return correctMarks ;
            }
            else {
                attempt.setEvaluationStatus( "INCORRECT" ) ;
                return wrongPenalty ;
            }
        }
        return 0 ;
    }
}
