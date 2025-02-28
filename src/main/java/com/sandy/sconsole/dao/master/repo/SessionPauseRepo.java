package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.SessionPause;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionPauseRepo extends JpaRepository<SessionPause, Integer> {
}
