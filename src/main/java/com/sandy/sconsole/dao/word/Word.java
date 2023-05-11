package com.sandy.sconsole.dao.word;

import com.sandy.sconsole.core.util.StringUtil;
import com.sandy.sconsole.core.wordnic.WordnikWord;
import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
@Entity
@Table(name = "word_master")
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String word ;
    private String meaning ;
    private String example ;
    private float  frequency ;

    private boolean   hidden          = false;
    private boolean   starred         = false;
    private boolean   wordnikEnriched = false ;
    private float     rating          = 0;
    private int       numShows        = 0;
    private Timestamp lastDisplayTime = null;

    private int numWordnikTries = 0 ;

    @OneToMany( cascade = CascadeType.ALL,
                mappedBy="word",
                fetch = FetchType.EAGER )
    private List<WordMeaning> meanings = new ArrayList<>() ;

    @OneToMany( cascade = CascadeType.ALL,
                mappedBy="word",
                fetch = FetchType.EAGER )
    private List<WordExample> examples = new ArrayList<>() ;

    public void populate( WordnikWord wordnikWord ) {

        if( !this.word.equalsIgnoreCase( wordnikWord.getWord() ) ) {
            throw new IllegalArgumentException( "Words don't match." );
        }

        if( meanings == null ) { meanings = new ArrayList<>() ; }
        if( examples == null ) { examples = new ArrayList<>() ; }

        wordnikWord.getMeanings().forEach( this::populateWordMeaning ) ;
        if( this.meaning != null ) {
            populateWordMeaning( this.meaning ) ;
        }
        else {
            this.meaning = meanings.get( 0 ).getMeaning() ;
        }

        wordnikWord.getExamples().forEach( this::populateWordExamples ) ;
        populateWordExamples( this.example ) ;
        if( this.example != null ) {
            populateWordExamples( this.example ) ;
        }
        else {
            if( !examples.isEmpty() ) {
                this.example = examples.get( 0 ).getExample() ;
            }
        }
    }

    private void populateWordMeaning( String meaning ) {

        if( StringUtil.isEmptyOrNull( meaning ) ) { return ; }

        meaning = StringUtils.capitalize( meaning.trim() ) ;
        meaning = meaning.replace( "<xref>", "" ) ;
        meaning = meaning.replace( "</xref>", "" ) ;

        boolean alreadyExists = false ;
        for( WordMeaning wm : meanings ) {
            if( wm.getMeaning().equals( meaning ) ) {
                alreadyExists = true ;
                break ;
            }
        }
        if( !alreadyExists ) {
            WordMeaning wm = new WordMeaning() ;
            wm.setWord( this ) ;
            wm.setMeaning( meaning ) ;
            meanings.add( wm ) ;
        }
    }

    private void populateWordExamples( String example ) {

        if( StringUtil.isEmptyOrNull( example ) ) { return ; }

        String enrichedExample = StringUtil.enrichExample( example, word ) ;

        boolean alreadyExists = false ;
        for( WordExample we : examples ) {
            if( we.getExample().equals( enrichedExample ) ) {
                alreadyExists = true ;
                break ;
            }
        }
        if( !alreadyExists ) {
            WordExample we = new WordExample() ;
            we.setWord( this ) ;
            we.setExample( enrichedExample ) ;
            examples.add( we ) ;
        }
    }

    public String toString() {
        return word ;
    }

}
