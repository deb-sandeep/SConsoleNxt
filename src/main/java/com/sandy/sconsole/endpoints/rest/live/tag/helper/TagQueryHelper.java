package com.sandy.sconsole.endpoints.rest.live.tag.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandy.sconsole.dao.exam.Question;
import com.sandy.sconsole.dao.exam.TagQuestionMap;
import com.sandy.sconsole.dao.exam.repo.QuestionRepo;
import com.sandy.sconsole.dao.master.SavedTagQuery;
import com.sandy.sconsole.dao.master.TagProblemMap;
import com.sandy.sconsole.dao.master.TopicProblem;
import com.sandy.sconsole.dao.master.repo.SavedTagQueryRepo;
import com.sandy.sconsole.dao.master.repo.TopicProblemRepo;
import com.sandy.sconsole.endpoints.rest.live.tag.vo.SavedTagQueryVO;
import com.sandy.sconsole.endpoints.rest.live.tag.vo.TagQueryConditionNode;
import com.sandy.sconsole.endpoints.rest.live.tag.vo.TagQueryGroupNode;
import com.sandy.sconsole.endpoints.rest.live.tag.vo.TagQueryNode;
import com.sandy.sconsole.endpoints.rest.live.tag.vo.reqres.SaveQueryReq;
import com.sandy.sconsole.endpoints.rest.live.tag.vo.reqres.TagBrowserFilters;
import com.sandy.sconsole.endpoints.rest.live.tag.vo.reqres.TagQuerySearchReq;
import com.sandy.sconsole.endpoints.rest.live.tag.vo.reqres.TagQuerySearchRes;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.QuestionVO;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Scope( "prototype" )
public class TagQueryHelper {

    @Autowired private TopicProblemRepo topicProblemRepo ;

    @Autowired private QuestionRepo questionRepo ;

    @Autowired private SavedTagQueryRepo savedTagQueryRepo ;

    @Autowired private ObjectMapper objectMapper ;

    @FunctionalInterface
    private interface TagExistsPredicateFactory {
        Predicate tagExists( CriteriaBuilder cb, CriteriaQuery<?> query, Integer tagId ) ;
    }

    public TagQuerySearchRes search( TagQuerySearchReq req ) {

        if( !(req.tagQuery() instanceof TagQueryGroupNode root) ) {
            throw new IllegalArgumentException( "tagQuery root must be a group node" ) ;
        }

        Specification<TopicProblem> problemSpec = Specification.where( tagQuerySpecForProblems( root ) )
                .and( problemFilterSpec( req.filters() ) ) ;
        Specification<Question> questionSpec = Specification.where( tagQuerySpecForQuestions( root ) )
                .and( questionFilterSpec( req.filters() ) ) ;

        List<TopicProblem> problems = topicProblemRepo.findAll( problemSpec ) ;
        List<Question> questions = questionRepo.findAll( questionSpec ) ;

        return new TagQuerySearchRes(
                problems,
                questions.stream().map( QuestionVO::new ).toList() ) ;
    }

    // ---------------------------------------------------------------------
    // Saved queries - a saved query is the whole search criteria (tagQuery +
    // filters) serialized to JSON, so recalling it reproduces the exact
    // search that was saved.
    // ---------------------------------------------------------------------

    public SavedTagQueryVO saveQuery( SaveQueryReq req ) {

        if( req.name() == null || req.name().isBlank() ) {
            throw new IllegalArgumentException( "name is required" ) ;
        }
        if( req.query() == null ) {
            throw new IllegalArgumentException( "query is required" ) ;
        }

        // Upsert by name: overwrite an existing saved query with the same
        // name rather than creating a duplicate.
        SavedTagQuery entity = savedTagQueryRepo.findByName( req.name() ).orElseGet( SavedTagQuery::new ) ;
        entity.setName( req.name() ) ;
        entity.setQuery( writeQueryJson( req.query() ) ) ;
        savedTagQueryRepo.save( entity ) ;

        return SavedTagQueryVO.from( entity ) ;
    }

    public void deleteQuery( Integer id ) {
        if( !savedTagQueryRepo.existsById( id ) ) {
            throw new IllegalArgumentException( "No saved query found with id: " + id ) ;
        }
        savedTagQueryRepo.deleteById( id ) ;
    }

    public List<SavedTagQueryVO> getSavedQueries() {
        return savedTagQueryRepo.findAllByOrderByNameAsc().stream()
                .map( SavedTagQueryVO::from )
                .toList() ;
    }

    public TagQuerySearchReq getQuery( Integer id ) {
        SavedTagQuery entity = savedTagQueryRepo.findById( id )
                .orElseThrow( () -> new IllegalArgumentException( "No saved query found with id: " + id ) ) ;
        return readQueryJson( entity.getQuery() ) ;
    }

    private String writeQueryJson( TagQuerySearchReq query ) {
        try {
            return objectMapper.writeValueAsString( query ) ;
        }
        catch( JsonProcessingException e ) {
            throw new IllegalStateException( "Failed to serialize query", e ) ;
        }
    }

    private TagQuerySearchReq readQueryJson( String json ) {
        try {
            return objectMapper.readValue( json, TagQuerySearchReq.class ) ;
        }
        catch( JsonProcessingException e ) {
            throw new IllegalStateException( "Failed to deserialize saved query", e ) ;
        }
    }

    // ---------------------------------------------------------------------
    // Boolean tag-tree evaluation, shared between Problems and Questions.
    // Each condition node becomes a correlated EXISTS/NOT EXISTS subquery
    // (supplied by existsFactory) against the relevant tag-map table; group
    // nodes combine children via AND/OR and optionally negate the result.
    // ---------------------------------------------------------------------

    private Predicate evalTagQueryNode( TagQueryNode node, CriteriaBuilder cb,
                                         CriteriaQuery<?> query, TagExistsPredicateFactory existsFactory ) {

        if( node instanceof TagQueryConditionNode c ) {
            Predicate exists = existsFactory.tagExists( cb, query, c.tagId() ) ;
            return c.negate() ? cb.not( exists ) : exists ;
        }

        TagQueryGroupNode g = (TagQueryGroupNode)node ;
        List<Predicate> childPredicates = g.children().stream()
                .map( child -> evalTagQueryNode( child, cb, query, existsFactory ) )
                .toList() ;

        Predicate combined ;
        if( childPredicates.isEmpty() ) {
            // Zero-child identity: AND -> true, OR -> false, per spec §4.1.
            combined = "AND".equals( g.op() ) ? cb.conjunction() : cb.disjunction() ;
        }
        else {
            combined = "AND".equals( g.op() )
                    ? cb.and( childPredicates.toArray( new Predicate[0] ) )
                    : cb.or( childPredicates.toArray( new Predicate[0] ) ) ;
        }
        return g.negated() ? cb.not( combined ) : combined ;
    }

    private Specification<TopicProblem> tagQuerySpecForProblems( TagQueryGroupNode tagQuery ) {
        return (root, query, cb) -> evalTagQueryNode( tagQuery, cb, query, (cb2, q2, tagId) -> {
            Subquery<Integer> sq = q2.subquery( Integer.class ) ;
            Root<TagProblemMap> subRoot = sq.from( TagProblemMap.class ) ;
            sq.select( subRoot.get( "id" ) ) ;
            sq.where(
                    cb2.equal( subRoot.get( "problem" ).get( "id" ), root.get( "problemId" ) ),
                    cb2.equal( subRoot.get( "tag" ).get( "id" ), tagId )
            ) ;
            return cb2.exists( sq ) ;
        } ) ;
    }

    private Specification<Question> tagQuerySpecForQuestions( TagQueryGroupNode tagQuery ) {
        return (root, query, cb) -> evalTagQueryNode( tagQuery, cb, query, (cb2, q2, tagId) -> {
            Subquery<Integer> sq = q2.subquery( Integer.class ) ;
            Root<TagQuestionMap> subRoot = sq.from( TagQuestionMap.class ) ;
            sq.select( subRoot.get( "id" ) ) ;
            sq.where(
                    cb2.equal( subRoot.get( "question" ).get( "id" ), root.get( "id" ) ),
                    cb2.equal( subRoot.get( "tag" ).get( "id" ), tagId )
            ) ;
            return cb2.exists( sq ) ;
        } ) ;
    }

    // ---------------------------------------------------------------------
    // Filters (§4.2) - plain AND on top of the tag-tree predicate.
    // ---------------------------------------------------------------------

    private Specification<TopicProblem> problemFilterSpec( TagBrowserFilters f ) {
        return Specification.where( problemSyllabusSpec( f.syllabusNames() ) )
                .and( problemTopicSpec( f.topicIds() ) )
                .and( problemDifficultySpec( f.difficultyMin() ) )
                .and( problemTimeAttemptsSpec( f ) ) ;
    }

    private Specification<TopicProblem> problemSyllabusSpec( List<String> names ) {
        return (root, q, cb) -> (names == null || names.isEmpty())
                ? cb.conjunction() : root.get( "syllabusName" ).in( names ) ;
    }

    private Specification<TopicProblem> problemTopicSpec( List<Integer> topicIds ) {
        return (root, q, cb) -> (topicIds == null || topicIds.isEmpty())
                ? cb.conjunction() : root.get( "topicId" ).in( topicIds ) ;
    }

    private Specification<TopicProblem> problemDifficultySpec( int min ) {
        return (root, q, cb) -> (min <= 0)
                ? cb.conjunction() : cb.ge( root.get( "difficultyLevel" ), min ) ;
    }

    private Specification<TopicProblem> problemTimeAttemptsSpec( TagBrowserFilters f ) {
        boolean timeActive = !(f.timeSpentMin() == 0 && f.timeSpentMax() == 30) ;
        boolean attemptsActive = !"any".equals( f.attempts() ) ;

        return (root, q, cb) -> {
            if( !timeActive && !attemptsActive ) return cb.conjunction() ;

            List<Predicate> preds = new ArrayList<>() ;
            // Either constraint active -> narrow to attempted problems first.
            preds.add( cb.gt( root.get( "numAttempts" ), 0L ) ) ;

            if( timeActive ) {
                preds.add( cb.between( root.get( "totalDuration" ),
                        BigDecimal.valueOf( f.timeSpentMin() * 60L ),
                        BigDecimal.valueOf( f.timeSpentMax() * 60L ) ) ) ;
            }
            if( attemptsActive ) {
                preds.add( attemptsPredicate( root, cb, f.attempts() ) ) ;
            }
            return cb.and( preds.toArray( new Predicate[0] ) ) ;
        } ;
    }

    private Predicate attemptsPredicate( Root<TopicProblem> root, CriteriaBuilder cb, String attempts ) {
        var numAttempts = root.<Long>get( "numAttempts" ) ;
        return switch( attempts ) {
            case "1"  -> cb.equal( numAttempts, 1L ) ;
            case "2+" -> cb.ge( numAttempts, 2L ) ;
            case "3+" -> cb.ge( numAttempts, 3L ) ;
            case "4+" -> cb.ge( numAttempts, 4L ) ;
            case "5+" -> cb.ge( numAttempts, 5L ) ;
            default   -> cb.conjunction() ;
        } ;
    }

    private Specification<Question> questionFilterSpec( TagBrowserFilters f ) {
        return Specification.where( questionSyllabusSpec( f.syllabusNames() ) )
                .and( questionTopicSpec( f.topicIds() ) )
                .and( questionDifficultySpec( f.difficultyMin() ) ) ;
    }

    private Specification<Question> questionSyllabusSpec( List<String> names ) {
        return (root, q, cb) -> (names == null || names.isEmpty())
                ? cb.conjunction() : root.get( "syllabus" ).get( "syllabusName" ).in( names ) ;
    }

    private Specification<Question> questionTopicSpec( List<Integer> topicIds ) {
        return (root, q, cb) -> (topicIds == null || topicIds.isEmpty())
                ? cb.conjunction() : root.get( "topic" ).get( "id" ).in( topicIds ) ;
    }

    private Specification<Question> questionDifficultySpec( int min ) {
        return (root, q, cb) -> (min <= 0)
                ? cb.conjunction() : cb.ge( root.get( "rating" ), min ) ;
    }
}
