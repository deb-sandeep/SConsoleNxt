package com.sandy.sconsole.endpoints.rest.master.tag.helper;

import com.sandy.sconsole.dao.exam.Question;
import com.sandy.sconsole.dao.exam.TagQuestionMap;
import com.sandy.sconsole.dao.exam.repo.QuestionRepo;
import com.sandy.sconsole.dao.exam.repo.TagQuestionMapRepo;
import com.sandy.sconsole.dao.master.Problem;
import com.sandy.sconsole.dao.master.TagMaster;
import com.sandy.sconsole.dao.master.TagProblemMap;
import com.sandy.sconsole.dao.master.repo.ProblemRepo;
import com.sandy.sconsole.dao.master.repo.TagProblemMapRepo;
import com.sandy.sconsole.dao.master.repo.TagRepo;
import com.sandy.sconsole.endpoints.rest.master.core.vo.ProblemVO;
import com.sandy.sconsole.endpoints.rest.master.tag.TaggableItemType;
import com.sandy.sconsole.endpoints.rest.master.tag.vo.QuestionSummaryVO;
import com.sandy.sconsole.endpoints.rest.master.tag.vo.TagVO;
import com.sandy.sconsole.endpoints.rest.master.tag.vo.reqres.TagAssociationRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@Scope( "prototype" )
public class TagAssociationHelper {

    @Autowired private TagRepo tagRepo ;

    @Autowired private ProblemRepo problemRepo ;

    @Autowired private QuestionRepo questionRepo ;

    @Autowired private TagProblemMapRepo tagProblemMapRepo ;

    @Autowired private TagQuestionMapRepo tagQuestionMapRepo ;

    public void addTag( TaggableItemType itemType, Integer itemId, Integer tagId ) {

        TagMaster tag = tagRepo.findById( tagId )
                .orElseThrow( () -> new IllegalArgumentException( "No tag found with id: " + tagId ) ) ;

        switch( itemType ) {
            case PROBLEM -> {
                Problem problem = problemRepo.findById( itemId )
                        .orElseThrow( () -> new IllegalArgumentException( "No problem found with id: " + itemId ) ) ;
                TagProblemMap map = new TagProblemMap() ;
                map.setProblem( problem ) ;
                map.setTag( tag ) ;
                tagProblemMapRepo.save( map ) ;
            }
            case QUESTION -> {
                Question question = questionRepo.findById( itemId )
                        .orElseThrow( () -> new IllegalArgumentException( "No question found with id: " + itemId ) ) ;
                TagQuestionMap map = new TagQuestionMap() ;
                map.setQuestion( question ) ;
                map.setTag( tag ) ;
                tagQuestionMapRepo.save( map ) ;
            }
        }
    }

    public void removeTag( TaggableItemType itemType, Integer itemId, Integer tagId ) {
        switch( itemType ) {
            case PROBLEM -> tagProblemMapRepo.deleteByProblemIdAndTagId( itemId, tagId ) ;
            case QUESTION -> tagQuestionMapRepo.deleteByQuestionIdAndTagId( itemId, tagId ) ;
        }
    }

    public List<TagVO> getTagsForItem( TaggableItemType itemType, Integer itemId ) {
        return switch( itemType ) {
            case PROBLEM -> tagProblemMapRepo.findByProblemId( itemId ).stream()
                    .map( m -> new TagVO( m.getTag() ) )
                    .toList() ;
            case QUESTION -> tagQuestionMapRepo.findByQuestionId( itemId ).stream()
                    .map( m -> new TagVO( m.getTag() ) )
                    .toList() ;
        } ;
    }

    public void removeAllTags( TaggableItemType itemType, Integer itemId ) {
        switch( itemType ) {
            case PROBLEM -> tagProblemMapRepo.deleteAllByProblemId( itemId ) ;
            case QUESTION -> tagQuestionMapRepo.deleteAllByQuestionId( itemId ) ;
        }
    }

    @Transactional
    public List<TagVO> setTags( TaggableItemType itemType, Integer itemId, List<Integer> targetTagIds ) {

        Set<Integer> currentTagIds = new HashSet<>() ;
        for( TagVO tag : getTagsForItem( itemType, itemId ) ) {
            currentTagIds.add( tag.getId() ) ;
        }
        Set<Integer> targetIds = new HashSet<>( targetTagIds ) ;

        for( Integer tagId : currentTagIds ) {
            if( !targetIds.contains( tagId ) ) {
                removeTag( itemType, itemId, tagId ) ;
            }
        }
        for( Integer tagId : targetIds ) {
            if( !currentTagIds.contains( tagId ) ) {
                addTag( itemType, itemId, tagId ) ;
            }
        }

        return getTagsForItem( itemType, itemId ) ;
    }

    public TagAssociationRes getItemsForTag( Integer tagId ) {

        List<ProblemVO> problems = tagProblemMapRepo.findByTagId( tagId ).stream()
                .map( m -> new ProblemVO( m.getProblem() ) )
                .toList() ;

        List<QuestionSummaryVO> questions = tagQuestionMapRepo.findByTagId( tagId ).stream()
                .map( m -> new QuestionSummaryVO( m.getQuestion() ) )
                .toList() ;

        return new TagAssociationRes( problems, questions ) ;
    }
}
