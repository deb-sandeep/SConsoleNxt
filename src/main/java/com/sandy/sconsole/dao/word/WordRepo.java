package com.sandy.sconsole.dao.word;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WordRepo extends JpaRepository<Word, Integer> {

    Word findByWord( String word ) ;

    List<Word> findTop100ByExampleIsNotNullOrderByFrequencyDesc() ;

    @Query( "SELECT w " +
            "FROM Word w " +
            "WHERE " +
            "  w.wordnikEnriched = true " +
            "ORDER BY " +
            "  w.numShows ASC," +
            "  w.frequency DESC " +
            "LIMIT 100"
    )
    List<Word> findProbableNextWords() ;

    @Query(
        "SELECT w " +
        "FROM Word w " +
        "WHERE " +
        "  w.wordnikEnriched = false AND " +
        "  w.numWordnikTries < 3 " +
        "ORDER BY " +
        "  w.frequency DESC, " +
        "  w.numWordnikTries ASC " +
        "LIMIT 1"
    )
    Word findWordForEnrichment() ;
}
