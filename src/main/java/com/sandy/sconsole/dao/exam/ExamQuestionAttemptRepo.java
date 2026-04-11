package com.sandy.sconsole.dao.exam;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ExamQuestionAttemptRepo extends JpaRepository<ExamQuestionAttempt, Integer> {
    
    @Lock( LockModeType.PESSIMISTIC_WRITE )
    @Query( "select eqa from ExamQuestionAttempt eqa where eqa.id = :id" )
    Optional<ExamQuestionAttempt> findByIdForUpdate( @Param( "id" ) Integer id ) ;
}
