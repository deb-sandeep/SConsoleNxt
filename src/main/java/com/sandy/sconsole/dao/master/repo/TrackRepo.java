package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.Track;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TrackRepo extends CrudRepository<Track, Integer> {

    List<Track> findAllByOrderByIdAsc() ;
}
