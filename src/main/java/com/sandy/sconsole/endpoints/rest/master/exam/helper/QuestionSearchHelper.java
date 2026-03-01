package com.sandy.sconsole.endpoints.rest.master.exam.helper;

import com.sandy.sconsole.dao.exam.Question;
import com.sandy.sconsole.dao.exam.repo.QuestionRepo;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.reqres.AvailableQuestionRes;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.reqres.QuestionSearchReq;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.reqres.QuestionSearchRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
@Scope( "prototype" )
public class QuestionSearchHelper {
    
    @Autowired
    private QuestionRepo questionRepo ;
    
    public QuestionSearchRes search( QuestionSearchReq req ) {
        Pageable pageCriteria = buildPagingCriteria( req ) ;
        var spec = buildSearchSpecs( req ) ;
        
        Page<Question> resultPage = questionRepo.findAll( spec, pageCriteria ) ;
        return QuestionSearchRes.from( resultPage ) ;
    }
    
    private Pageable buildPagingCriteria( QuestionSearchReq req ) {
        int page = (req.page() == null) ? 0 : req.page() ;
        int pageSize = (req.size() == null ) ? 0 : req.size() ;
        
        Sort sortObj = Sort.unsorted() ;
        if( req.sort() != null && !req.sort().isEmpty() ) {
            sortObj = Sort.by( req.sort().stream().map( this::parseSearchOrder ).toList() ) ;
        }
        return PageRequest.of( page, pageSize, sortObj );
    }
    
    private Sort.Order parseSearchOrder( String s ) {
        String[] parts = s.split( ":", 2 ) ;
        String field = parts[0].trim() ;
        String dir = (parts.length == 2) ? parts[1].trim().toLowerCase() : "asc" ;
        return "desc".equals( dir ) ? Sort.Order.desc( field ) : Sort.Order.asc( field ) ;
    }
    
    private Specification<Question> buildSearchSpecs( QuestionSearchReq req ) {
        return Specification.where( hasAnyTopicIds( req.topicIds() ) ) ;
    }
    
    private Specification<Question> hasAnyTopicIds( List<Integer> topicIds ) {
        return (root, query, cb) ->  {
            if( topicIds == null || topicIds.isEmpty() ) return cb.conjunction() ;
            return root.get( "topic" ).get( "id" ).in( topicIds ) ;
        } ;
    }
    
    public AvailableQuestionRes getAvailableQuestions( int topicId, String[] problemTypes ) {
        return new AvailableQuestionRes( topicId, new HashMap<>() ) ;
    }
}
