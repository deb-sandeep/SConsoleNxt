package com.sandy.sconsole.dao.test.repo;

import com.sandy.sconsole.dao.test.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepo extends JpaRepository<Question, Integer> {
}
