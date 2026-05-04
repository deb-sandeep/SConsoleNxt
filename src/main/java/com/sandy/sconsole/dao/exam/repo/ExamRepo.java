package com.sandy.sconsole.dao.exam.repo;

import com.sandy.sconsole.dao.exam.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExamRepo extends JpaRepository<Exam, Integer> {

    @Query( nativeQuery = true,
            value = "SELECT name FROM exam_qattempt_lap_obs_master" )
    List<String> findAllLapAnalysisObservationNames() ;
}
