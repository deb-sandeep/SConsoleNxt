package com.sandy.sconsole.dao.session.repo;

import com.sandy.sconsole.dao.session.DaySyllabusStudyTime;
import com.sandy.sconsole.dao.session.DaySyllabusStudyTimeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface DaySyllabusStudyTimeRepo extends
        JpaRepository<DaySyllabusStudyTime, DaySyllabusStudyTimeId> {
    
    @Query( """
        select t
        from DaySyllabusStudyTime t
        where t.id.date >= :startDate and t.id.syllabusName = :syllabusName
    """)
    List<DaySyllabusStudyTime> getStudyTimesFromDate(
            @Param( "startDate" ) final Date startDate,
            @Param( "syllabusName" ) final String syllabusName ) ;
}
