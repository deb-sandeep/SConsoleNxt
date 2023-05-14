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
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Component
public class RefresherSlideManager implements ComponentInitializer {

    @Autowired private SlideRepo slideRepo ;

    private final Map<String, ChapterSlideCluster> clusterMap = new TreeMap<>() ;
    private ChapterSlideCluster lastShownCluster = null ;

    @Override
    public int getInitializationSequencePreference() {
        return 2000 ;
    }

    @Override
    public void initialize( SConsole app ) throws Exception {

        final Timestamp[] lastShownTimestamp = {null} ;

        clusterMap.clear() ;

        slideRepo.findAll().forEach( s -> {

            ChapterSlideCluster cluster = getCluster( s ) ;
            cluster.add( s ) ;

            if( s.getLastDisplayTime() != null ) {
                if( lastShownTimestamp[0] == null ||
                    s.getLastDisplayTime().after( lastShownTimestamp[0] ) ) {

                    lastShownTimestamp[0] = s.getLastDisplayTime() ;
                    lastShownCluster = cluster ;
                }
            }
        } ) ;

        if( lastShownCluster == null && !clusterMap.isEmpty() ) {
            lastShownCluster = clusterMap.values()
                                         .toArray( new ChapterSlideCluster[0] )[0] ;
        }
    }

    public void add( Slide s ) {
        getCluster( s ).add( s ) ;
    }

    public void delete( Slide s ) {
        getCluster( s ).delete( s ) ;
    }

    private String getKey( Slide s ) {
        return s.getSyllabus() + "/" + s.getSubject() + "/" + s.getChapter() ;
    }

    private ChapterSlideCluster getCluster( Slide s ) {
        return clusterMap.computeIfAbsent( getKey( s ),
                             k -> new ChapterSlideCluster( s.getSyllabus(),
                                                           s.getSubject(),
                                                           s.getChapter() ) ) ;
    }
}
