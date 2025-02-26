package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepo extends JpaRepository<Session, Integer> {
}
