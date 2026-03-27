package com.sandy.sconsole.dao.exam.repo;

import com.sandy.sconsole.dao.exam.ExamEventLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamEventLogRepo extends JpaRepository<ExamEventLog, Integer> {

    List<ExamEventLog> findByExamAttemptIdOrderBySequenceAsc( Integer examAttemptId ) ;
}
