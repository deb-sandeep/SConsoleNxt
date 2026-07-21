package com.sandy.sconsole.dao.session.repo;

import com.sandy.sconsole.dao.session.DailyBurnLog;
import com.sandy.sconsole.dao.session.DailyBurnLogId;
import com.sandy.sconsole.ui.util.DayValueChart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface DailyBurnLogRepo extends JpaRepository<DailyBurnLog, DailyBurnLogId> {

    interface DateBurnMet {
        Date getDate() ;
        // MySQL's JDBC driver reports the type of MIN()/MAX() over a
        // TINYINT(1) column inconsistently depending on query shape - a
        // plain query returns Byte, a joined one (as used by
        // getSyllabusFullBurnMet) returns Boolean. Spring Data's projection
        // proxy won't convert between either of those and a fixed declared
        // type, so these stay Object and the asBoolean() helpers below
        // normalize whichever shows up.
        // (Note: these can't be named isFullBurnMet()/isAnyOverrideUsed() -
        // Spring Data's projection proxy treats "is"/"get" as interchangeable
        // prefixes for the same JavaBean property, so they would collide
        // with getFullBurnMet()/getAnyOverrideUsed() and break the proxy's
        // accessor resolution.)
        Object getFullBurnMet() ;
        Object getAnyOverrideUsed() ;

        default boolean burnMetAsBoolean() {
            Object raw = getFullBurnMet() ;
            if( raw instanceof Boolean b ) return b ;
            if( raw instanceof Number n )  return n.intValue() != 0 ;
            return false ;
        }

        default boolean anyOverrideUsedAsBoolean() {
            Object raw = getAnyOverrideUsed() ;
            if( raw instanceof Boolean b ) return b ;
            if( raw instanceof Number n )  return n.intValue() != 0 ;
            return false ;
        }

        default DayValueChart.BurnMetStatus toBurnMetStatus() {
            if( !burnMetAsBoolean() ) return DayValueChart.BurnMetStatus.UNMET ;
            return anyOverrideUsedAsBoolean()
                    ? DayValueChart.BurnMetStatus.MET_VIA_OVERRIDE
                    : DayValueChart.BurnMetStatus.MET_NATURAL ;
        }
    }

    @Query( nativeQuery = true, value = """
        select
            dbl.date as date,
            min( case when dbl.required_burn_met = 1 or dbl.burn_met_override = 1
                    then 1
                    else 0
                end ) as fullBurnMet,
            max( case when dbl.required_burn_met = 0 and dbl.burn_met_override = 1
                    then 1
                    else 0
                end ) as anyOverrideUsed
        from daily_burn_log dbl
            join topic_master tm
                on tm.id = dbl.topic_id
        where
            tm.syllabus_name = :syllabusName and
            dbl.date between :startDate and :endDate
        group by
            dbl.date
    """ )
    List<DateBurnMet> getSyllabusFullBurnMet( @Param( "syllabusName" ) String syllabusName,
                                              @Param( "startDate" ) Date startDate,
                                              @Param( "endDate" ) Date endDate ) ;

    @Query( nativeQuery = true, value = """
        select
            date,
            min( case when required_burn_met = 1 or burn_met_override = 1
                    then 1
                    else 0
                end ) as fullBurnMet,
            max( case when required_burn_met = 0 and burn_met_override = 1
                    then 1
                    else 0
                end ) as anyOverrideUsed
        from daily_burn_log
        where
            date between :startDate and :endDate
        group by
            date
    """ )
    List<DateBurnMet> getTotalFullBurnMet( @Param( "startDate" ) Date startDate,
                                           @Param( "endDate" ) Date endDate ) ;
}
