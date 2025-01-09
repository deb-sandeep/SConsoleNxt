package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.Book;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BookRepo extends CrudRepository<Book, Integer> {
    
    List<Book> findBySubject_SubjectNameIgnoreCase( String subjectName ) ;
    
    @Query( """
            select b
            from Book b
            where
                upper(b.subject.subjectName) = upper(?1) and
                upper(b.bookName) = upper(?2) and
                upper(b.author) = upper(?3)
            """
    )
    Book findBook( String subjectName, String bookName, String author );
}
