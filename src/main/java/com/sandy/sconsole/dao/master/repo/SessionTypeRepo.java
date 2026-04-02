package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.SessionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SessionTypeRepo extends JpaRepository<SessionType, Integer> {
    
    SessionType findBySessionType( String sessionType ) ;
    
    @Query( """
        select st
        from SessionType st
        where st.automated = false
    """)
    List<SessionType> findAllNonAutomated() ;
}
