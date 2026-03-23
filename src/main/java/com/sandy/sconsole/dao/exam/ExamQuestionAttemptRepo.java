package com.sandy.sconsole.dao.exam;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamQuestionAttemptRepo extends JpaRepository<ExamQuestionAttempt, Integer> {
    
    List<ExamQuestionAttempt> findAllByExamSectionAttempt( ExamSectionAttempt sectionAttempt ) ;
}
