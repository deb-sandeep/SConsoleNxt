package com.sandy.sconsole.daemon.refresher.internal;

import com.sandy.sconsole.dao.slide.SlideVO;

import java.util.Comparator;

public class SlideDisplayPriorityComparator implements Comparator<SlideVO> {

    @Override
    public int compare( SlideVO s1, SlideVO s2 ) {

        // A positive value implies s2 has been shown recently than s1
        int absDisplayTimeGapDiff = (int)Math.abs( s1.getSecondsSinceLastDisplay() -
                                                   s2.getSecondsSinceLastDisplay() ) ;

        // If both the slides have not been shown even once, show them in
        // ascending order of their slide names. Else prioritize any slide
        // which is yet to be shown
        if( s1.getNumShows() == 0 && s2.getNumShows() == 0 ) {
            return s1.getSlideName().compareTo( s2.getSlideName() ) ;
        }
        else if( s1.getNumShows() == 0 ) { return -1 ; }
        else if( s2.getNumShows() == 0 ) { return 1 ; }

        // If the display time gap is more than two days, the slide which
        // has been recently shown will have low display priority
        if( absDisplayTimeGapDiff > 2*24*60 ) {
            return (int)(s2.getSecondsSinceLastDisplay() - s1.getSecondsSinceLastDisplay()) ;
        }

        // If the display time is within a two day range, prioritize the slide
        // which has been shown fewer number of times
        return s1.getNumShows() - s2.getNumShows() ;
    }
}
