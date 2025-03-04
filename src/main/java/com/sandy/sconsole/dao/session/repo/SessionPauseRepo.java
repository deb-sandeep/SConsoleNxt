package com.sandy.sconsole.dao.session.repo;

import com.sandy.sconsole.dao.session.SessionPause;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionPauseRepo extends JpaRepository<SessionPause, Integer> {
}
