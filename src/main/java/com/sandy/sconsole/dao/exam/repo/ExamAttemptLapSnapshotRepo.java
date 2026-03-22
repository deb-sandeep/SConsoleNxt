package com.sandy.sconsole.dao.exam.repo;

import com.sandy.sconsole.dao.exam.ExamAttemptLapSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamAttemptLapSnapshotRepo
        extends JpaRepository<ExamAttemptLapSnapshot, Integer> {
}
