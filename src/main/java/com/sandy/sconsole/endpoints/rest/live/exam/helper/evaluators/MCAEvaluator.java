package com.sandy.sconsole.endpoints.rest.live.exam.helper.evaluators;

import com.sandy.sconsole.dao.exam.ExamQuestion;
import com.sandy.sconsole.dao.exam.ExamQuestionAttempt;
import com.sandy.sconsole.dao.exam.ExamSection;
import com.sandy.sconsole.endpoints.rest.live.exam.helper.SectionEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@Scope( "prototype" )
public class MCAEvaluator extends SectionEvaluator {

    /**
     * This method is called only if the question questionAttempt is either ANSWERED
     * or ANS_AND_MARKED_FOR_REVIEW, so a provided answer is guaranteed to
     * be present.
     *
     * MCA questions may have more than one correct option. Both the correct
     * answer and the submitted answer are comma separated tokens (e.g. "A,C").
     * If any selected option is incorrect, the full wrong penalty applies.
     * If the selected options are a correct but partial subset of the answer
     * key, partial marks equal to the number of correctly selected options
     * are awarded. An exact match earns full marks.
     */
    @Override
    protected int evaluateQuestionAttempt( ExamSection section,
                                           ExamQuestion question,
                                           ExamQuestionAttempt questionAttempt ) {

        int correctMarks = section.getCorrectMarks() ;
        int wrongPenalty = section.getWrongPenalty() ;

        String answer = questionAttempt.getAnswerProvided() ;
        String correctAnswer = question.getQuestion().getAnswer() ;

        if( answer != null && !answer.isEmpty() ) {

            Set<String> selectedOptions = toOptionSet( answer ) ;
            Set<String> correctOptions  = toOptionSet( correctAnswer ) ;

            if( correctOptions.containsAll( selectedOptions ) ) {
                if( selectedOptions.equals( correctOptions ) ) {
                    questionAttempt.setEvaluationStatus( "CORRECT" ) ;
                    return correctMarks ;
                }
                else {
                    questionAttempt.setEvaluationStatus( "PARTIAL" ) ;
                    return selectedOptions.size() ;
                }
            }
        }
        questionAttempt.setEvaluationStatus( "INCORRECT" ) ;
        return wrongPenalty ;
    }

    private Set<String> toOptionSet( String csv ) {
        Set<String> options = new HashSet<>() ;
        for( String token : csv.split( "," ) ) {
            options.add( token.trim().toUpperCase() ) ;
        }
        return options ;
    }
}
