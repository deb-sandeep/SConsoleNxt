package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.Book;
import com.sandy.sconsole.dao.master.Syllabus;
import com.sandy.sconsole.dao.master.SyllabusBookMap;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface SyllabusBookMapRepo extends CrudRepository<SyllabusBookMap, Integer> {
    
    @Query( """
        select sbm.syllabus
        from SyllabusBookMap sbm
        where sbm.book = :book
    """)
    Syllabus findByBook( @Param( "book" ) Book book ) ;
}
