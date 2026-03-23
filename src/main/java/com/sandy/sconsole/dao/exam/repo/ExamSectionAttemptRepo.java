package com.sandy.sconsole.dao.exam.repo;

import com.sandy.sconsole.dao.exam.ExamAttempt;
import com.sandy.sconsole.dao.exam.ExamSectionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamSectionAttemptRepo extends JpaRepository<ExamSectionAttempt, Integer> {
    
    List<ExamSectionAttempt> findAllByExamAttempt( ExamAttempt attempt ) ;
}
