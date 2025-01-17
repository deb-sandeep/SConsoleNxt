package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.Chapter;
import com.sandy.sconsole.dao.master.ChapterId;
import org.springframework.data.repository.CrudRepository;

public interface ChapterRepo extends CrudRepository<Chapter, ChapterId> {
}
