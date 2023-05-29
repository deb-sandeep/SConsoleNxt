package com.sandy.sconsole.daemon.refresher.internal;

import com.sandy.sconsole.dao.slide.SlideVO;

import java.util.*;

public class ChapterSlideCluster {

    private final String syllabus ;
    private final String subject ;
    private final String chapter ;

    private int currentSlideIndex = -1 ;
    private final List<SlideVO> slides = new ArrayList<>() ;

    private final SlideDisplayPriorityComparator slideComparator = new SlideDisplayPriorityComparator() ;

    public ChapterSlideCluster( String syllabus, String subject, String chapter ) {
        this.syllabus = syllabus ;
        this.subject = subject ;
        this.chapter = chapter ;
    }

    public void add( SlideVO s ) {
        slides.add( s ) ;
        slides.sort( slideComparator ) ;
    }

    public void delete( SlideVO s ) {
        for( int i=0; i<slides.size(); i++ ) {
            if( slides.get( i ).getSlideName().equals( s.getSlideName() ) ) {
                slides.remove( i ) ;
                break ;
            }
        }
        slides.sort( slideComparator ) ;
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

    public int getAvgNonShowDelayInSeconds() {
        if( slides.isEmpty() ) return 0 ;

        int totalNonShowDelayInMinutes = 0 ;
        for( SlideVO slide : slides ) {
            totalNonShowDelayInMinutes += slide.getSecondsSinceLastDisplay() ;
        }
        return (totalNonShowDelayInMinutes / slides.size()) ;
    }

    public int getMinimumSlideShows() {

        int minNumShows = Integer.MAX_VALUE ;
        for( SlideVO slide : slides ) {
            minNumShows = Math.min( slide.getNumShows(), minNumShows ) ;
        }
        return minNumShows ;
    }

    public String toString() {
        return getKey() + ". Avg show delay = " + getAvgNonShowDelayInSeconds() ;
    }
}
