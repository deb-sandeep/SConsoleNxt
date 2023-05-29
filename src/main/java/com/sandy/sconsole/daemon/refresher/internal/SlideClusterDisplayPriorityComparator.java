package com.sandy.sconsole.daemon.refresher.internal;

import java.util.Comparator;

public class SlideClusterDisplayPriorityComparator
        implements Comparator<ChapterSlideCluster> {

    @Override
    public int compare( ChapterSlideCluster c1, ChapterSlideCluster c2 ) {

        return -( c1.getAvgNonShowDelayInSeconds() -
                  c2.getAvgNonShowDelayInSeconds() ) ;
    }
}
