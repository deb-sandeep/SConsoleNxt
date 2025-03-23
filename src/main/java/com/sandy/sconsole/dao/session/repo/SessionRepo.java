package com.sandy.sconsole.dao.session.repo;

import com.sandy.sconsole.dao.session.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface SessionRepo extends JpaRepository<Session, Integer> {
    
    @Query( nativeQuery=true, value = """
        SELECT *
        FROM
            session s
        WHERE
           ( s.start_time > CURDATE() AND s.start_time < DATE_ADD( CURDATE(), INTERVAL 1 DAY ) ) OR
           ( s.end_time > CURDATE() AND s.end_time < DATE_ADD( CURDATE(), INTERVAL 1 DAY ) )
        ORDER BY id
    """ )
    List<Session> getTodaySessions() ;
    
    @Query( nativeQuery=true, value = """
        SELECT *
        FROM
            session s
        WHERE
           ( s.start_time > CAST(:date as DATE) AND s.start_time < DATE_ADD( CAST(:date as DATE), INTERVAL 1 DAY ) ) OR
           ( s.end_time > CAST(:date as DATE) AND s.end_time < DATE_ADD( CAST(:date as DATE), INTERVAL 1 DAY ) )
        ORDER BY id
    """ )
    List<Session> getSessionsForDate( @Param( "date" ) Date date ) ;
    
    @Query( nativeQuery=true, value = """
        SELECT *
        FROM
            session s
        WHERE
            s.start_time > DATE(DATE_SUB(NOW(), INTERVAL 31 DAY))
        ORDER BY id
    """ )
    List<Session> getL30Sessions() ;
}
