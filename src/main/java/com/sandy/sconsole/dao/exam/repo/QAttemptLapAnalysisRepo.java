package com.sandy.sconsole.dao.exam.repo;

import com.sandy.sconsole.dao.exam.QAttemptLapAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QAttemptLapAnalysisRepo extends JpaRepository<QAttemptLapAnalysis, Integer> {

    Optional<QAttemptLapAnalysis> findByAttemptIdAndLapName(
            Integer attemptId, String lapName ) ;

    @Query( """
        SELECT a
        FROM QAttemptLapAnalysis a
            LEFT JOIN FETCH a.observations
        WHERE a.attempt.id = :attemptId
    """ )
    List<QAttemptLapAnalysis> findByAttemptIdWithObservations(
            @Param( "attemptId" ) Integer attemptId ) ;
}
