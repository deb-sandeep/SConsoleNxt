package com.sandy.sconsole.endpoints.rest.master.core.helper;

import com.sandy.sconsole.dao.exam.TagQuestionMap;
import com.sandy.sconsole.dao.exam.repo.TagQuestionMapRepo;
import com.sandy.sconsole.dao.master.TagMaster;
import com.sandy.sconsole.dao.master.TagProblemMap;
import com.sandy.sconsole.dao.master.Topic;
import com.sandy.sconsole.dao.master.repo.TagProblemMapRepo;
import com.sandy.sconsole.dao.master.repo.TagRepo;
import com.sandy.sconsole.dao.master.repo.TopicRepo;
import com.sandy.sconsole.endpoints.rest.master.core.vo.TagVO;
import com.sandy.sconsole.endpoints.rest.master.core.vo.reqres.TagCreateReq;
import com.sandy.sconsole.endpoints.rest.master.core.vo.reqres.TagRenameReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Component
@Scope( "prototype" )
public class TagHelper {

    @Autowired private TagRepo tagRepo ;

    @Autowired private TopicRepo topicRepo ;

    @Autowired private TagProblemMapRepo tagProblemMapRepo ;

    @Autowired private TagQuestionMapRepo tagQuestionMapRepo ;

    public TagVO createTag( TagCreateReq req ) {

        String normalizedText = normalize( req.tagText() ) ;
        if( tagRepo.findByNormalizedTagText( normalizedText ).isPresent() ) {
            throw new IllegalArgumentException(
                    "A tag with this text already exists: " + req.tagText() ) ;
        }

        Topic topic = topicRepo.findById( req.topicId() )
                .orElseThrow( () -> new IllegalArgumentException(
                        "No topic found with id: " + req.topicId() ) ) ;

        TagMaster tag = new TagMaster() ;
        tag.setTagText( req.tagText().trim() ) ;
        tag.setNormalizedTagText( normalizedText ) ;
        tag.setTopic( topic ) ;
        tag.setCreatedAt( Instant.now() ) ;

        return new TagVO( tagRepo.save( tag ) ) ;
    }

    public TagVO renameTag( Integer tagId, TagRenameReq req ) {

        TagMaster tag = tagRepo.findById( tagId )
                .orElseThrow( () -> new IllegalArgumentException(
                        "No tag found with id: " + tagId ) ) ;

        String normalizedText = normalize( req.newTagText() ) ;
        Optional<TagMaster> existing = tagRepo.findByNormalizedTagText( normalizedText ) ;
        if( existing.isPresent() && !existing.get().getId().equals( tagId ) ) {
            throw new IllegalArgumentException(
                    "A tag with this text already exists: " + req.newTagText() ) ;
        }

        tag.setTagText( req.newTagText().trim() ) ;
        tag.setNormalizedTagText( normalizedText ) ;

        return new TagVO( tagRepo.save( tag ) ) ;
    }

    @Transactional
    public void mergeTags( Integer sourceTagId, Integer targetTagId ) {

        if( sourceTagId.equals( targetTagId ) ) {
            throw new IllegalArgumentException( "Cannot merge a tag into itself." ) ;
        }

        TagMaster targetTag = tagRepo.findById( targetTagId )
                .orElseThrow( () -> new IllegalArgumentException(
                        "No tag found with id: " + targetTagId ) ) ;

        if( !tagRepo.existsById( sourceTagId ) ) {
            throw new IllegalArgumentException( "No tag found with id: " + sourceTagId ) ;
        }

        for( TagProblemMap map : tagProblemMapRepo.findByTagId( sourceTagId ) ) {
            if( tagProblemMapRepo.existsByProblemIdAndTagId( map.getProblem().getId(), targetTagId ) ) {
                tagProblemMapRepo.delete( map ) ;
            }
            else {
                map.setTag( targetTag ) ;
                tagProblemMapRepo.save( map ) ;
            }
        }

        for( TagQuestionMap map : tagQuestionMapRepo.findByTagId( sourceTagId ) ) {
            if( tagQuestionMapRepo.existsByQuestionIdAndTagId( map.getQuestion().getId(), targetTagId ) ) {
                tagQuestionMapRepo.delete( map ) ;
            }
            else {
                map.setTag( targetTag ) ;
                tagQuestionMapRepo.save( map ) ;
            }
        }

        tagRepo.deleteById( sourceTagId ) ;
    }

    private String normalize( String tagText ) {
        return tagText.trim().toLowerCase() ;
    }
}
