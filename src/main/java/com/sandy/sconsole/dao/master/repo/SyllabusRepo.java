package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.Book;
import com.sandy.sconsole.dao.master.Subject;
import com.sandy.sconsole.dao.master.Syllabus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SyllabusRepo extends CrudRepository<Syllabus, String> {
    
    @Query( "SELECT sbm.book " +
            "FROM SyllabusBookMap sbm " +
            "WHERE " +
            "  sbm.syllabus.syllabusName = :syllabusName "
    )
    List<Book> findBooksForSyllabus( String syllabusName ) ;
    
    List<Syllabus> findBySubject( Subject subject ) ;
}
