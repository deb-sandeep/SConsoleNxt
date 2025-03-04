package com.sandy.sconsole.dao.session.repo;

import com.sandy.sconsole.dao.session.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepo extends JpaRepository<Session, Integer> {
}
