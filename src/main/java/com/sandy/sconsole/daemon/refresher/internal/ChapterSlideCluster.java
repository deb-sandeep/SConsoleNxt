package com.sandy.sconsole.daemon.refresher.internal;

import com.sandy.sconsole.dao.slide.SlideVO;

import java.util.*;

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
        sortAllSLides() ;
    }

    public void delete( SlideVO s ) {
        for( int i=0; i<slides.size(); i++ ) {
            if( slides.get( i ).getSlideName().equals( s.getSlideName() ) ) {
                slides.remove( i ) ;
                break ;
            }
        }
        sortAllSLides() ;
    }

    private void sortAllSLides() {
        slides.sort( Comparator.comparing( SlideVO::getMinutesSinceLastDisplay ) ) ;
        Collections.reverse( slides ) ;
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

    public long getAverageShowDelayInMinutes() {
        if( slides.isEmpty() ) return 0 ;

        long totalNonShowDelay = 0 ;
        for( SlideVO slide : slides ) {
            totalNonShowDelay += slide.getMinutesSinceLastDisplay() ;
        }
        return (totalNonShowDelay / slides.size()) ;
    }

    public String toString() {
        return getKey() + ". Avg show delay = " + getAverageShowDelayInMinutes() ;
    }
}
