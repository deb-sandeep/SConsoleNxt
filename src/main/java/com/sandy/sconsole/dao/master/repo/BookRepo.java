package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.api.master.dto.BookSummary;
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
    """ )
    Book findBook( String subjectName, String bookName, String author ) ;
    
    @Query( """
        select b.id as id,
               b.subject.subjectName as subjectName,
               b.seriesName as seriesName,
               b.bookName as bookName,
               b.author as author,
               b.bookShortName as bookShortName,
               count( distinct( c ) ) as numChapters,
               count( p ) as numProblems
        from Book b
            left outer join Chapter c
                on b = c.book
            left outer join Problem p
                on b = p.chapter.book and
                   c = p.chapter
        group by
            b.id
        order by
            b.subject.subjectName asc,
            b.seriesName asc,
            b.author asc
    """ )
    List<BookSummary> findAllBooks() ;
}
