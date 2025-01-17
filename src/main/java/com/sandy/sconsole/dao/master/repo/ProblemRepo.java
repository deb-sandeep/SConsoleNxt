package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.Problem;
import org.springframework.data.repository.CrudRepository;

public interface ProblemRepo extends CrudRepository<Problem, Integer> {
}
