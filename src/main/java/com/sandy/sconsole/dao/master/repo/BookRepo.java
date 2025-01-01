package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.Book;
import org.springframework.data.repository.CrudRepository;

public interface BookRepo extends CrudRepository<Book, Integer> {
}
