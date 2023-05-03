package com.sandy.sconsole.dao.quote;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface QuoteRepo extends CrudRepository<Quote, Integer> {
    List<Quote> findBySpeaker( String speaker ) ;
}
