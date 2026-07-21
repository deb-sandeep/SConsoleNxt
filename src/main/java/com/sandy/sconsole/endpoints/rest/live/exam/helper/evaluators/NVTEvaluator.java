package com.sandy.sconsole.endpoints.rest.live.exam.helper.evaluators;

import com.sandy.sconsole.dao.exam.ExamQuestion;
import com.sandy.sconsole.dao.exam.ExamQuestionAttempt;
import com.sandy.sconsole.dao.exam.ExamSection;
import com.sandy.sconsole.endpoints.rest.live.exam.helper.SectionEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@Scope( "prototype" )
public class NVTEvaluator extends SectionEvaluator {

    private static final BigDecimal NVT_TOLERANCE = new BigDecimal( "0.01" ) ;

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

            // BigDecimal parses any valid decimal literal without throwing, so a
            // decimal-formatted answer to an IVT question is simply evaluated as
            // incorrect (zero tolerance) rather than crashing the evaluation.
            BigDecimal refAnswer = new BigDecimal( correctAnswer ) ;
            BigDecimal submittedAnswer = new BigDecimal( answer ) ;
            BigDecimal tolerance = "IVT".equals( problemType ) ? BigDecimal.ZERO : NVT_TOLERANCE ;

            boolean isCorrect = refAnswer.subtract( submittedAnswer ).abs()
                                         .compareTo( tolerance ) <= 0 ;

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
