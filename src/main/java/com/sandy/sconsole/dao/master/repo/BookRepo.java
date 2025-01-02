package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.Book;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BookRepo extends CrudRepository<Book, Integer> {
    
    List<Book> findBySubject_SubjectNameIgnoreCase( String subjectName );
}
