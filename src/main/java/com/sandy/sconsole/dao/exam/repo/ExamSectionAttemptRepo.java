package com.sandy.sconsole.dao.exam.repo;

import com.sandy.sconsole.dao.exam.ExamSectionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamSectionAttemptRepo extends JpaRepository<ExamSectionAttempt, Integer> {
}
