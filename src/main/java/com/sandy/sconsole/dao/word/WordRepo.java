package com.sandy.sconsole.dao.word;

import org.springframework.data.repository.CrudRepository;

public interface WordRepo extends CrudRepository<Word, Integer> {

    public Word findByWord( String word ) ;
}
