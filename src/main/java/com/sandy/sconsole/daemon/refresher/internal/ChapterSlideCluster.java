package com.sandy.sconsole.daemon.refresher.internal;

import com.sandy.sconsole.dao.slide.SlideVO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChapterSlideCluster {

    private final String syllabus ;
    private final String subject ;
    private final String chapter ;

    private int currentSlideIndex = -1 ;
    private final List<SlideVO> slides = new ArrayList<>() ;

    public ChapterSlideCluster( String syllabus, String subject, String chapter ) {
        this.syllabus = syllabus ;
        this.subject = subject ;
        this.chapter = chapter ;
    }

    public void add( SlideVO s ) {
        slides.add( s ) ;
        slides.sort( Comparator.comparing( SlideVO::getSlideName ) ) ;
    }

    public void delete( SlideVO s ) {
        for( int i=0; i<slides.size(); i++ ) {
            if( slides.get( i ).getSlideName().equals( s.getSlideName() ) ) {
                slides.remove( i ) ;
                break ;
            }
        }
    }

    public SlideVO getNextSlide() {
        currentSlideIndex++ ;
        if( currentSlideIndex >= slides.size() ) {
            currentSlideIndex=-1 ;
            return null ;
        }
        return slides.get( currentSlideIndex ) ;
    }

    public String getKey() {
        return syllabus + "/" + subject + "/" + chapter ;
    }

    public long getAverageShowDelay() {
        if( slides.isEmpty() ) return 0 ;

        long totalNonShowDelay = 0 ;
        long now = System.currentTimeMillis() ;
        for( SlideVO slide : slides ) {
            if( slide.getLastDisplayTime() == null ) {
                // If a slide has not been displayed ever, assume it was
                // last shown ten year back.
                totalNonShowDelay += 31_536_0000L * 1000L ;
            }
            else {
                totalNonShowDelay += ( now - slide.getLastDisplayTime().getTime() ) ;
            }
        }
        return (totalNonShowDelay / slides.size()) ;
    }
}
