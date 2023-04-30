package com.sandy.sconsole.dao.quote.repo;

import com.sandy.sconsole.dao.quote.Quote;
import org.springframework.data.repository.CrudRepository;

public interface QuoteRepo extends CrudRepository<Quote, Integer> {
}
