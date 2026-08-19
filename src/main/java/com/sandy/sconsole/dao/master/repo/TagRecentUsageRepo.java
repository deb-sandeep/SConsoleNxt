package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.TagRecentUsage;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TagRecentUsageRepo extends CrudRepository<TagRecentUsage, Integer> {

    List<TagRecentUsage> findAllByOrderByLastUsedAtDesc() ;
}
