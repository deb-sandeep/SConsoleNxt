package com.sandy.sconsole.dao.word;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WordRepo extends JpaRepository<Word, Integer> {

    Word findByWord( String word ) ;

    List<Word> findTop100ByExampleIsNotNullOrderByFrequencyDesc() ;

    @Query( "SELECT w " +
            "FROM Word w " +
            "ORDER BY " +
            "  w.numShows ASC," +
            "  w.frequency DESC " +
            "LIMIT 100"
    )
    List<Word> findProbableNextWords() ;
}
