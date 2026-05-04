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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            }
        }

        // Recompute exec score as the integer average of scores across all laps.
        // Re-fetching from the DB (rather than using the in-memory collection) ensures
        // the deleted lap is excluded and the newly inserted lap is included.
        List<QAttemptLapAnalysis> allLaps = qAttemptLapAnalysisRepo.findByAttemptId( qAttemptId ) ;
        int execScore = (int) Math.round(
                allLaps.stream()
                       .mapToInt( l -> l.getScore() == null ? 0 : l.getScore() )
                       .average()
                       .orElse( 0.0 ) ) ;

        attempt.setExecScore( execScore ) ;
        examQuestionAttemptRepo.save( attempt ) ;

        return new QAttemptLapAnalysisUpdateRes(
                attempt.getId(), req.lapName(), execScore ) ;
    }
}
