package com.sandy.sconsole.daemon.refresher;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.behavior.ComponentInitializer;
import com.sandy.sconsole.daemon.refresher.internal.ChapterSlideCluster;
import com.sandy.sconsole.dao.slide.Slide;
import com.sandy.sconsole.dao.slide.SlideRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Component
public class RefresherSlideManager implements ComponentInitializer {

    @Autowired private SlideRepo slideRepo ;

    // Syllabus -> Subject -> Chapter -> ChapterSlideCluster
    private final Map<String, Map<String, Map<String, ChapterSlideCluster>>> clusterMap = new TreeMap<>() ;

    private final List<ChapterSlideCluster> clusters = new ArrayList<>() ;
    private ChapterSlideCluster currentCluster = null ;

    @Override
    public int getInitializationSequencePreference() {
        return 99 ;
    }

    @Override
    public void initialize( SConsole app ) throws Exception {

        log.debug( "Initializing RefresherSlideManager." ) ;

        clusterMap.clear() ;
        slideRepo.findAll().forEach( s -> {
            ChapterSlideCluster cluster = getCluster( s ) ;
            cluster.add( s ) ;
        } ) ;

        sortClusters() ;

        // Pick the cluster that has the minimum projection score. This
        // cluster will have slides that have been shown the minimum.
        if( !clusters.isEmpty() ) {
            currentCluster = clusters.get( 0 ) ;
        }
    }

    void add( Slide s ) {
        getCluster( s ).add( s ) ;
        sortClusters() ;
    }

    void delete( Slide s ) {
        getCluster( s ).delete( s ) ;
        sortClusters() ;
    }

    public Slide getNextSlide() {

        if( clusters.isEmpty() ) return null ;

        Slide nextSlide = null ;

        if( currentCluster != null ) {
            nextSlide = currentCluster.getNextSlide() ;
        }

        if( nextSlide == null ) {
            sortClusters() ;
            currentCluster = clusters.get( 0 ) ;
            return getNextSlide() ;
        }

        updateSlideState( nextSlide ) ;

        return nextSlide ;
    }

    private void updateSlideState( Slide inputSlide ) {

        Timestamp timestamp = new Timestamp( System.currentTimeMillis() ) ;

        Slide slide = slideRepo.findById( inputSlide.getId() ).get() ;
        slide.setNumShows( slide.getNumShows()+1 ) ;
        slide.setLastDisplayTime( timestamp ) ;
        slideRepo.save( slide ) ;

        inputSlide.setNumShows( inputSlide.getNumShows()+1 ) ;
        inputSlide.setLastDisplayTime( timestamp ) ;
    }

    private void sortClusters() {
        // Cluster with the largest average show delay is given higher
        // precedence to be shown first.
        clusters.sort( (c1, c2) -> ( int ) ( c2.getAverageShowDelay() -
                                             c1.getAverageShowDelay() ) ) ;
    }

    private ChapterSlideCluster getCluster( Slide s ) {

        Map<String, Map<String, ChapterSlideCluster>> syllabusClusterMap ;
        Map<String, ChapterSlideCluster> chapterClusterMap ;
        ChapterSlideCluster chapterCluster ;

        syllabusClusterMap = clusterMap.computeIfAbsent( s.getSyllabus(),
                                                      k -> new TreeMap<>() ) ;

        chapterClusterMap = syllabusClusterMap.computeIfAbsent( s.getSubject(),
                                                          k -> new TreeMap<>() ) ;

        chapterCluster = chapterClusterMap.get( s.getChapter() ) ;
        if( chapterCluster == null ) {
            chapterCluster = new ChapterSlideCluster( s.getSyllabus(),
                                                      s.getSubject(),
                                                      s.getChapter() ) ;
            chapterClusterMap.put( s.getChapter(), chapterCluster ) ;
            clusters.add( chapterCluster ) ;
        }
        return chapterCluster ;
    }
}
