package com.sandy.sconsole.daemon.refresher.internal;

import com.sandy.sconsole.dao.slide.Slide;

import java.util.ArrayList;
import java.util.List;

public class ChapterSlideCluster {

    private final String syllabus ;
    private final String subject ;
    private final String chapter ;

    private final List<Slide> slides = new ArrayList<>() ;

    public ChapterSlideCluster( String syllabus, String subject, String chapter ) {
        this.syllabus = syllabus ;
        this.subject = subject ;
        this.chapter = chapter ;
    }

    public void add( Slide s ) {
        slides.add( s ) ;
        slides.sort( (s1, s2) -> s1.getSlideName().compareTo( s2.getSlideName() ) ) ;
    }

    public void delete( Slide s ) {
        for( int i=0; i<slides.size(); i++ ) {
            if( slides.get( i ).getSlideName().equals( s.getSlideName() ) ) {
                slides.remove( i ) ;
                break ;
            }
        }
    }

    public String getKey() {
        return syllabus + "/" + subject + "/" + chapter ;
    }
}
