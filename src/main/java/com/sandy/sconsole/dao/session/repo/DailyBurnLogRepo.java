package com.sandy.sconsole.dao.session.repo;

import com.sandy.sconsole.dao.session.DailyBurnLog;
import com.sandy.sconsole.dao.session.DailyBurnLogId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface DailyBurnLogRepo extends JpaRepository<DailyBurnLog, DailyBurnLogId> {

    interface DateBurnMet {
        Date getDate() ;
        // MySQL returns MIN() over a TINYINT(1) column as a Byte, not a
        // Boolean - Spring Data's projection proxy won't auto-convert that,
        // so this is intentionally Byte; callers compare against zero.
        Byte getFullBurnMet() ;
    }

    @Query( nativeQuery = true, value = """
        select dbl.date as date, min(dbl.required_burn_met) as fullBurnMet
        from daily_burn_log dbl
        join topic_master tm on tm.id = dbl.topic_id
        where tm.syllabus_name = :syllabusName
          and dbl.date between :startDate and :endDate
        group by dbl.date
    """ )
    List<DateBurnMet> getSyllabusFullBurnMet( @Param( "syllabusName" ) String syllabusName,
                                              @Param( "startDate" ) Date startDate,
                                              @Param( "endDate" ) Date endDate ) ;

    @Query( nativeQuery = true, value = """
        select date, min(required_burn_met) as fullBurnMet
        from daily_burn_log
        where date between :startDate and :endDate
        group by date
    """ )
    List<DateBurnMet> getTotalFullBurnMet( @Param( "startDate" ) Date startDate,
                                           @Param( "endDate" ) Date endDate ) ;
}
