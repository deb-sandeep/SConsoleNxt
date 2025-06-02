package com.sandy.sconsole.dao.audit.repo;

import com.sandy.sconsole.dao.audit.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EventLogRepo extends JpaRepository<EventLog, Integer> {
    
    @Query( """
    select el
    from EventLog el
    where date(el.time) = CURDATE()
    """)
    List<EventLog> findEventLogsForToday() ;
}
