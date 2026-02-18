package com.sandy.sconsole.dao.exam.repo;

import com.sandy.sconsole.dao.exam.ExamEventLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamEventLogRepo extends JpaRepository<ExamEventLog, Integer> {
}
