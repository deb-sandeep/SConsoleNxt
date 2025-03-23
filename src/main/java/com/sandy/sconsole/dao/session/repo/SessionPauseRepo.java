package com.sandy.sconsole.dao.session.repo;

import com.sandy.sconsole.dao.session.SessionPause;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface SessionPauseRepo extends JpaRepository<SessionPause, Integer> {
    
    @Query( nativeQuery=true, value = """
        SELECT *
        FROM
            session_pause p
        WHERE
           ( p.start_time > CURDATE() AND p.start_time < DATE_ADD( CURDATE(), INTERVAL 1 DAY ) ) OR
           ( p.end_time > CURDATE() AND p.end_time < DATE_ADD( CURDATE(), INTERVAL 1 DAY ) )
        ORDER BY id
    """ )
    List<SessionPause> getTodayPauses() ;
    
    @Query( nativeQuery=true, value = """
        SELECT *
        FROM
            session_pause p
        WHERE
           ( p.start_time > CAST(:date as DATE) AND p.start_time < DATE_ADD( CAST(:date as DATE), INTERVAL 1 DAY ) ) OR
           ( p.end_time > CAST(:date as DATE) AND p.end_time < DATE_ADD( CAST(:date as DATE), INTERVAL 1 DAY ) )
        ORDER BY id
    """ )
    List<SessionPause> getPausesForDate( @Param( "date" ) Date date ) ;
    
    @Query( nativeQuery=true, value = """
        SELECT *
        FROM
            session_pause p
        WHERE
            p.start_time > DATE(DATE_SUB(NOW(), INTERVAL 31 DAY))
        ORDER BY id
    """ )
    List<SessionPause> getL30SessionPauses() ;
}
