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
     *
     * An NVT section can contain both NVT (real valued) and IVT (integer valued)
     * questions, so the comparison rule is picked per-question based on the
     * question's own problem type rather than the section's.
     */
    @Override
    protected int evaluateQuestionAttempt( ExamSection section,
                                           ExamQuestion question,
                                           ExamQuestionAttempt questionAttempt ) {

        int correctMarks = section.getCorrectMarks() ;
        int wrongPenalty = section.getWrongPenalty() ;

        String answer = questionAttempt.getAnswerProvided() ;
        String correctAnswer = question.getQuestion().getAnswer() ;
        String problemType = question.getQuestion().getProblemType().getProblemType() ;

        if( answer != null &&
            !answer.isEmpty() ) {

            boolean isCorrect ;
            if( "IVT".equals( problemType ) ) {
                Integer refAnswer = Integer.parseInt( correctAnswer ) ;
                Integer submittedAnswer = Integer.parseInt( answer ) ;
                isCorrect = refAnswer.equals( submittedAnswer ) ;
            }
            else {
                Float refAnswer = Float.parseFloat( correctAnswer ) ;
                Float submittedAnswer = Float.parseFloat( answer ) ;
                isCorrect = Math.abs( refAnswer - submittedAnswer ) <= TOLERANCE ;
            }

            if( isCorrect ) {
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
