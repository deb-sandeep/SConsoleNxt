package com.sandy.sconsole.dao.exam.repo;

import com.sandy.sconsole.dao.exam.QAttemptLapAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QAttemptLapAnalysisRepo extends JpaRepository<QAttemptLapAnalysis, Integer> {

    Optional<QAttemptLapAnalysis> findByAttemptIdAndLapName( Integer attemptId, String lapName ) ;

    List<QAttemptLapAnalysis> findByAttemptId( Integer attemptId ) ;
}