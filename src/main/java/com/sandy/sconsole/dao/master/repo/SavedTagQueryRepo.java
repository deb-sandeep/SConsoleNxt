package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.SavedTagQuery;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface SavedTagQueryRepo extends CrudRepository<SavedTagQuery, Integer> {

    Optional<SavedTagQuery> findByName( String name ) ;

    List<SavedTagQuery> findAllByOrderByNameAsc() ;
}
