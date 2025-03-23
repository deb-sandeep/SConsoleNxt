package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.SessionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionTypeRepo extends JpaRepository<SessionType, Integer> {
    
    SessionType findBySessionType( String sessionType );
}
