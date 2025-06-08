package com.sandy.sconsole.dao.audit.repo;

import com.sandy.sconsole.dao.audit.SessionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SessionEventRepo extends JpaRepository<SessionEvent, Integer> {
  
  @Query( """
    select se
    from SessionEvent se
    where DATE(se.time) = CURDATE()
    """)
  List<SessionEvent> findEventsForToday() ;
}
