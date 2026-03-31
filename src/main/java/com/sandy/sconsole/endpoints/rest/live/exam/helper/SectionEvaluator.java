package com.sandy.sconsole.endpoints.rest.live.exam.helper;

import com.sandy.sconsole.dao.exam.ExamQuestion;
import com.sandy.sconsole.dao.exam.ExamQuestionAttempt;
import com.sandy.sconsole.dao.exam.ExamSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public abstract class SectionEvaluator {
    
    /**
     * Evaluates the question attempts for the section. Note that the
     * actual evaluation is delegated to the concrete implementation.
     *
     * Evaluates a section with a constraint of attempting any N out of M questions.
     *
     * <p>A question is considered "attempted" only if a valid answer has been explicitly
     * saved by the candidate using either "ANSWERED" or "ANS_AND_MARKED_FOR_REVIEW".
     * Merely visiting a question, marking it for review without an answer, or clearing
     * a response does not qualify as an attempt, and such questions are excluded from evaluation.</p>
     *
     * <p>The evaluation logic is strictly based on the count and order of answered questions.
     * Let A be the number of questions with saved answers:</p>
     *
     * <ul>
     *   <li>If A ≤ N: All answered questions are evaluated. Unanswered questions are ignored
     *       and do not attract any penalty.</li>
     *   <li>If A > N: Only the first N answered questions, in ascending order of the question
     *       sequence within the section, are considered for evaluation. Any additional
     *       answered questions beyond the first N are ignored entirely, irrespective of
     *       correctness.</li>
     * </ul>
     *
     * <p>Important: The system does not select the best N answers. It strictly evaluates
     * the first N answered questions based on their positional order. As a result, answers
     * submitted later in the sequence may be excluded from evaluation even if they are correct.</p>
     *
     * <p>This behavior implies that over-attempting questions in such sections can lead to
     * unintended exclusion of potentially correct answers. Therefore, both the presence of
     * a saved answer and the sequence in which answers are submitted are critical factors
     * in determining the final evaluated set.</p>
     */
    public final int evaluateSectionAttempt(
            ExamSection section,
            Set<ExamQuestionAttempt> questionAttempts ) {
        
        final int numCompulsoryQuestions = section.getNumCompulsoryQuestions() ;
        
        int totalSectionScore = 0 ;
        int numQuestionsEvaluated = 0 ;
        
        List<ExamQuestion> questions = new ArrayList<>( section.getQuestions() ) ;
        
        for( int i=0; i<questions.size(); i++ ) {
            
            ExamQuestion question = questions.get( i ) ;
            ExamQuestionAttempt questionAttempt = findQuestionAttempt( question, questionAttempts ) ;
            
            if( questionAttempt != null ) {
                
                // If the question has to be evaluated, the section evaluator
                // will populate the score and loss.
                questionAttempt.setScore( 0 ) ;
                questionAttempt.setLoss( 0 ) ;
                
                if( numQuestionsEvaluated < numCompulsoryQuestions ) {
                    
                    // By default, mark the question attempt as unanswered, and
                    // the loss is treated as avoidable. This will be changed
                    // when a root cause is assigned.
                    questionAttempt.setEvaluationStatus( "UNANSWERED" ) ;
                    questionAttempt.setLoss( section.getCorrectMarks() ) ;
                    questionAttempt.setAvoidableLoss( section.getCorrectMarks() ) ;
                    
                    String submitStatus = questionAttempt.getAnswerSubmitStatus() ;
                    if( submitStatus.equals( "ANSWERED" ) ||
                        submitStatus.equals( "ANS_AND_MARKED_FOR_REVIEW" ) ) {
                        
                        // By default, mark the question attempt as incorrect.
                        // The evaluator will override this if required. This is
                        // more of a fallback.
                        questionAttempt.setEvaluationStatus( "INCORRECT" ) ;
                        
                        int score = evaluateQuestionAttempt( section, question, questionAttempt );
                        int loss = section.getCorrectMarks() - score ;
                        
                        questionAttempt.setScore( score );
                        questionAttempt.setLoss( loss ) ;
                        questionAttempt.setAvoidableLoss( loss ) ;
                        
                        totalSectionScore += score;
                        numQuestionsEvaluated++ ;
                    }
                }
                else {
                    questionAttempt.setEvaluationStatus( "EVALUATION_SKIPPED" ) ;
                }
            }
            else {
                throw new IllegalStateException( "Question attempt not found for examQuestion " +
                                                 question.getId() ) ;
            }
        }
        return totalSectionScore ;
    }
    
    private ExamQuestionAttempt findQuestionAttempt(
            ExamQuestion question,
            Set<ExamQuestionAttempt> attempts ) {
        
        for( ExamQuestionAttempt attempt : attempts ) {
            if( Objects.equals( attempt.getExamQuestion().getId(), question.getId() ) ) {
                return attempt ;
            }
        }
        return null ;
    }
    
    /**
     * It is expected that the concrete implementation populates the evaluationStatus
     * and score in the questionAttempt instance. The persistence will be
     * taken care of by the superclass logic, and hence the implementation should
     * not try to persist the entity.
     *
     * @return The evaluation score for the question attempt.
     */
    protected abstract int evaluateQuestionAttempt(
            ExamSection section,
            ExamQuestion question,
            ExamQuestionAttempt questionAttempt ) ;
}
