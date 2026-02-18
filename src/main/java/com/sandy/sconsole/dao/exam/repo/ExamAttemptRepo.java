package com.sandy.sconsole.dao.exam.repo;

import com.sandy.sconsole.dao.exam.ExamAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamAttemptRepo extends JpaRepository<ExamAttempt, Integer> {
}
