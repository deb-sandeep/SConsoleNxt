package com.sandy.sconsole.dao.exam.repo;

import com.sandy.sconsole.dao.exam.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamRepo extends JpaRepository<Exam, Integer> {
}
