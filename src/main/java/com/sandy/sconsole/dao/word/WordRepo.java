package com.sandy.sconsole.dao.word;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface WordRepo extends CrudRepository<Word, Integer> {

    Word findByWord( String word ) ;

    List<Word> findTop100ByOrderByFrequencyDescNumShowsAsc() ;

    List<Word> findTop100ByExampleIsNotNullOrderByFrequencyDesc() ;
}
