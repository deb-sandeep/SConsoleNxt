package com.sandy.sconsole.dao.word;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordRepo extends JpaRepository<Word, Integer> {

    Word findByWord( String word ) ;

    List<Word> findTop100ByOrderByFrequencyDescNumShowsAsc() ;

    List<Word> findTop100ByExampleIsNotNullOrderByFrequencyDesc() ;

    List<Word> findTop10ByWordnikEnrichedIsTrueOrderByNumShowsAscFrequencyDesc() ;

    Word findFirstByWordnikEnrichedIsFalseOrderByFrequencyDesc() ;
}
