package com.sandy.sconsole.dao.session.repo;

import com.sandy.sconsole.dao.session.ProblemAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemAttemptRep extends JpaRepository<ProblemAttempt, Integer> {
}
