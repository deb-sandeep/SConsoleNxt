package com.sandy.sconsole.dao.exam.repo;

import com.sandy.sconsole.dao.exam.ExamAttemptLapSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamAttemptLapSnapshotRepo
        extends JpaRepository<ExamAttemptLapSnapshot, Integer> {
    
    List<ExamAttemptLapSnapshot> findByExamAttemptIdAndExamQuestionIdOrderByIdAsc(
            Integer examAttemptId, Integer examQuestionId ) ;
}
