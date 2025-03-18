package com.sandy.sconsole.dao.session.repo;

import com.sandy.sconsole.dao.session.DayStudyTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface DayStudyTimeRepo
        extends JpaRepository<DayStudyTime, LocalDate> {
    
    @Query( """
        select t
        from DayStudyTime t
        where t.date >= :startDate
    """)
    List<DayStudyTime> getStudyTimesFromDate( @Param( "startDate" ) final Date startDate ) ;
}
