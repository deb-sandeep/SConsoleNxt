package com.sandy.sconsole.endpoints.rest.live.exam.helper;

import com.sandy.sconsole.dao.exam.*;
import com.sandy.sconsole.dao.exam.repo.*;
import com.sandy.sconsole.endpoints.rest.live.exam.vo.QAttemptLapAnalysisUpdateReq;
import com.sandy.sconsole.endpoints.rest.live.exam.vo.QAttemptLapAnalysisUpdateRes;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.reqres.CreateExamAttemptRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Component
@Scope( "prototype" )
public class ExamAttemptHelper {
    
    @Autowired
    private ExamRepo examRepo ;

    @Autowired
    private ExamAttemptRepo examAttemptRepo ;
    
    @Autowired
    private ExamSectionAttemptRepo examSectionAttemptRepo ;
    
    @Autowired
    private ExamQuestionAttemptRepo examQuestionAttemptRepo ;

    @Autowired
    private QAttemptLapAnalysisRepo qAttemptLapAnalysisRepo ;

    @Autowired
    private QAttemptLapObsRepo qAttemptLapObsRepo ;
    
    @Transactional
    public CreateExamAttemptRes createExamAttempt( int examId ) {
        Exam exam = examRepo.findById( examId ).get() ;
        Map<Integer, Integer> questionAttemptIds = new HashMap<>() ;
        
        ExamAttempt examAttempt = createExamAttempt( exam ) ;
        
        for( ExamSection section : exam.getSections() ) {
            ExamSectionAttempt esAttempt = createExamSectionAttempt( examAttempt, section ) ;
            examAttempt.getSectionAttempts().add( esAttempt ) ;
            
            for( ExamQuestion question : section.getQuestions() ) {
                ExamQuestionAttempt questionAttempt = createExamQuestionAttempt( question, esAttempt ) ;
                esAttempt.getQuestionAttempts().add( questionAttempt ) ;
                
                questionAttemptIds.put( question.getId(), questionAttempt.getId() ) ;
            }
        }
        
        exam.setState( "IN_PROGRESS" ) ;
        examRepo.saveAndFlush( exam ) ;
        
        return new CreateExamAttemptRes( examId, examAttempt.getId(), questionAttemptIds ) ;
    }
    
    private ExamAttempt createExamAttempt( Exam exam ) {
        
        ExamAttempt examAttempt = new ExamAttempt() ;
        examAttempt.setExam( exam ) ;
        examAttempt.setAttemptDate( new Date().toInstant() ) ;
        examAttempt.setScore( 0 ) ;
        examAttempt.setLoss( 0 ) ;
        examAttempt.setAvoidableLoss( 0 ) ;
        examAttempt.setAvoidableLossPct( 0F ) ;
        examAttempt.setStatus( "IN_PROGRESS" ) ;
        return examAttemptRepo.saveAndFlush( examAttempt ) ;
    }
    
    private ExamSectionAttempt createExamSectionAttempt(
            ExamAttempt examAttempt, ExamSection section ) {
    
        ExamSectionAttempt esAttempt = new ExamSectionAttempt() ;
        esAttempt.setExamSection( section ) ;
        esAttempt.setExamAttempt( examAttempt ) ;
        esAttempt.setScore( 0 ) ;
        esAttempt.setLoss( 0 ) ;
        esAttempt.setAvoidableLoss( 0 ) ;
        esAttempt.setAvoidableLossPct( 0F ) ;
        return examSectionAttemptRepo.saveAndFlush( esAttempt ) ;
    }
    
    private ExamQuestionAttempt createExamQuestionAttempt(
            ExamQuestion question, ExamSectionAttempt esAttempt ) {

        ExamQuestionAttempt questionAttempt = new ExamQuestionAttempt() ;
        questionAttempt.setExamQuestion( question ) ;
        questionAttempt.setExamSectionAttempt( esAttempt ) ;
        questionAttempt.setTimeSpent( 0 ) ;
        questionAttempt.setEvaluationStatus( "UNANSWERED" ) ;
        questionAttempt.setAnswerProvided( null ) ;
        questionAttempt.setAnswerSubmitStatus( "NOT_VISITED" ) ;
        questionAttempt.setRootCause( null ) ;
        questionAttempt.setScore( 0 ) ;
        questionAttempt.setLoss( 0 ) ;
        questionAttempt.setAvoidableLoss( 0 ) ;
        questionAttempt.setExecScore( 0 ) ;
        return examQuestionAttemptRepo.saveAndFlush( questionAttempt ) ;
    }
    
    /**
     * Creates or replaces the lap-level analysis entry for a question attempt.
     *
     * <p>A question attempt can be worked on across multiple named laps (e.g. L1, L2).
     * Each lap may have exactly one {@link QAttemptLapAnalysis} record capturing a score,
     * a note, and zero or more tagged observations. This method enforces that invariant
     * by deleting any pre-existing analysis for the same attempt + lap before inserting
     * the new one supplied in the request.
     *
     * <p>After persisting the new analysis the method recomputes the parent attempt's
     * {@code execScore} as the integer average of scores across all its lap analyses.
     * This gives the coach a single composite execution quality indicator on the attempt.
     *
     * @param req  the update payload — must have a non-blank {@code lapName} and a valid
     *             {@code qAttemptId} that references an existing {@link ExamQuestionAttempt}
     * @return     a response containing the new analysis id and the recomputed exec score
     *
     * @throws IllegalArgumentException if {@code req} is null, {@code lapName} is blank,
     *                                  or no attempt exists for {@code qAttemptId}
     */
    @Transactional
    public QAttemptLapAnalysisUpdateRes saveQAttemptLapAnalysis(
            int qAttemptId, QAttemptLapAnalysisUpdateReq req ) {

        if( req.lapName() == null || req.lapName().isBlank() )
            throw new IllegalArgumentException( "lapName must not be blank" ) ;

        // Pessimistic write lock on the attempt prevents concurrent exec-score updates
        // from racing against each other when multiple laps are saved simultaneously.
        ExamQuestionAttempt attempt = examQuestionAttemptRepo
                .findByIdForUpdate( qAttemptId )
                .orElseThrow( () -> new IllegalArgumentException(
                        "ExamQuestionAttempt not found for id: " + qAttemptId ) ) ;

        // Remove existing analysis for this lap (if any).
        // The DB-level ON DELETE CASCADE on exam_qattempt_lap_obs.analysis_id
        // automatically removes all child observation rows.
        qAttemptLapAnalysisRepo
                .findByAttemptIdAndLapName( qAttemptId, req.lapName() )
                .ifPresent( existing -> qAttemptLapAnalysisRepo.delete( existing ) ) ;

        // Flush the delete so it is visible to the re-fetch below.
        qAttemptLapAnalysisRepo.flush() ;

        QAttemptLapAnalysis analysis = new QAttemptLapAnalysis() ;
        analysis.setAttempt( attempt ) ;
        analysis.setLapName( req.lapName() ) ;
        analysis.setScore( req.score() ) ;
        analysis.setNote( req.note() ) ;
        analysis = qAttemptLapAnalysisRepo.saveAndFlush( analysis ) ;

        if( req.observations() != null ) {
            for( String text : req.observations() ) {
                QAttemptLapObs obs = new QAttemptLapObs() ;
                obs.setAnalysis( analysis ) ;
                obs.setObservation( text ) ;
                qAttemptLapObsRepo.save( obs ) ;
                // Keep the in-memory collection consistent so the L1 cache holds
                // accurate state when getExecScore re-fetches with JOIN FETCH.
                analysis.getObservations().add( obs ) ;
            }
            qAttemptLapObsRepo.flush() ;
        }

        // Recompute exec score as the integer average of scores across all laps.
        // Re-fetching from the DB (rather than using the in-memory collection) ensures
        // the deleted lap is excluded and the newly inserted lap is included.
        int execScore = getExecScore( attempt ) ;
        attempt.setExecScore( execScore ) ;
        examQuestionAttemptRepo.save( attempt ) ;

        return new QAttemptLapAnalysisUpdateRes(
                attempt.getId(), req.lapName(), execScore ) ;
    }
    
    /**
     * Computes the execution quality score for a question attempt as a weighted
     * integer average (1–10) of its lap-level scores.
     *
     * <p><b>Weighting rules:</b>
     * <ul>
     *   <li><b>Commit lap</b> (lapName == {@code answerSubmitLap}) — weight 2.
     *       The commit lap is <em>never</em> excluded: if it is also tagged
     *       {@code ACCIDENTAL_TOUCH} the human observation is assumed to be mistaken
     *       and the hard data takes precedence.</li>
     *   <li><b>Accidental-touch lap</b> (non-commit lap whose observations contain
     *       {@code ACCIDENTAL_TOUCH}) — weight 0 (excluded entirely).</li>
     *   <li><b>All other laps</b> — weight 1.</li>
     * </ul>
     *
     * <p>Returns 0 when no laps survive the filter (e.g. every non-commit lap is
     * accidental, and no answer was ever committed).
     *
     * @param questionAttempt the attempt whose exec score is being recomputed;
     *                        {@code answerSubmitLap} may be null for unanswered questions
     *
     * @return weighted-average score rounded to the nearest integer, or 0 if no
     *         scoreable laps exist
     */
    private int getExecScore( ExamQuestionAttempt questionAttempt ) {

        // Re-fetch from DB with observations to ensure in-flight changes are reflected.
        List<QAttemptLapAnalysis> allLaps =
                qAttemptLapAnalysisRepo.findByAttemptIdWithObservations( questionAttempt.getId() ) ;

        String commitLapName = questionAttempt.getAnswerSubmitLap() ;

        int weightedSum = 0 ;
        int weightSum   = 0 ;

        for( QAttemptLapAnalysis lap : allLaps ) {
            boolean isCommitLap = commitLapName != null &&
                                  commitLapName.equals( lap.getLapName() ) ;
            
            // Commit lap is never excluded — hard data overrides any ACCIDENTAL_TOUCH tag.
            if( !isCommitLap ) {
                boolean isAccidental = false ;
                Set<QAttemptLapObs> observations = lap.getObservations() ;
                for( QAttemptLapObs obs : observations ) {
                    if( "ACCIDENTAL_TOUCH".equals( obs.getObservation() ) ) {
                        isAccidental = true ;
                        break ;
                    }
                }
                if( isAccidental ) continue ;
            }

            int weight = isCommitLap ? 2 : 1 ;
            int score  = lap.getScore() == null ? 0 : lap.getScore() ;
            
            weightedSum += weight * score ;
            weightSum   += weight ;
        }

        if( weightSum == 0 ) return 0 ;
        return (int) Math.round( (double) weightedSum / weightSum ) ;
    }
}
