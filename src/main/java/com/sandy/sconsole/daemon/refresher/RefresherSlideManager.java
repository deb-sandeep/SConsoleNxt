package com.sandy.sconsole.daemon.refresher;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.SConsoleConfig;
import com.sandy.sconsole.core.behavior.ComponentInitializer;
import com.sandy.sconsole.daemon.refresher.internal.ChapterSlideCluster;
import com.sandy.sconsole.dao.slide.Slide;
import com.sandy.sconsole.dao.slide.SlideRepo;
import com.sandy.sconsole.dao.slide.SlideVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Component
public class RefresherSlideManager implements ComponentInitializer {

    // This lock is shared between the slide manager and the refresher
    // daemon. Remember that a refresher daemon while executing a pull
    // request can delete files and hence can potentially cause an
    // invalid slide reference to be returned by this class. The object
    // prevents this.
    public static final Object LOCK = new Object() ;

    @Autowired private SlideRepo slideRepo ;
    @Autowired private SConsoleConfig appCfg ;

    // Syllabus -> Subject -> Chapter -> ChapterSlideCluster
    private final Map<String, Map<String, Map<String, ChapterSlideCluster>>> clusterMap = new TreeMap<>() ;
    private final List<ChapterSlideCluster> allClusters = new ArrayList<>() ;
    private ChapterSlideCluster currentCluster = null ;

    @Override
    public int getInitializationSequencePreference() {
        // Initialize this before ScreenManagerInitializer since slides
        // will be required for refresher screen.
        return 99 ;
    }

    @Override
    public void initialize( SConsole app ) throws Exception {

        synchronized( LOCK ) {
            log.debug( "Initializing RefresherSlideManager." ) ;

            clusterMap.clear() ;
            slideRepo.findAll().forEach( s -> {
                SlideVO vo = s.getVO() ;
                getCluster( vo ).add( vo ) ;
            } ) ;

            sortAllClusters() ;

            // Pick the cluster that has the minimum projection score. This
            // cluster will have slides that have been shown the minimum.
            if( !allClusters.isEmpty() ) {
                currentCluster = allClusters.get( 0 ) ;
            }
        }
    }

    void add( SlideVO s ) {
        getCluster( s ).add( s ) ;
        sortAllClusters() ;
    }

    void delete( SlideVO s ) {
        getCluster( s ).delete( s ) ;
        sortAllClusters() ;
    }

    public SlideVO getNextSlide() throws Exception {

        synchronized( LOCK ) {
            if( allClusters.isEmpty() ) return null ;

            SlideVO nextSlide = null ;

            if( currentCluster != null ) {
                nextSlide = currentCluster.getNextSlide() ;
            }

            if( nextSlide == null ) {
                sortAllClusters() ;
                currentCluster = allClusters.get( 0 ) ;
                return getNextSlide() ;
            }

            updateSlideState( nextSlide ) ;

            // Why do we set the image? To ensure that the slide object
            // is self-contained before existing the synchronized block.
            // Imagine leaving with a reference to a file while the
            // daemon removes it in the background.
            File file = new File( appCfg.getWorkspacePath(),
                                  "Refreshers/" + nextSlide.getPath() ) ;
            nextSlide.setImage( ImageIO.read( file ) ) ;

            return nextSlide ;
        }
    }

    private void updateSlideState( SlideVO slideVO ) {

        Timestamp timestamp = new Timestamp( System.currentTimeMillis() ) ;

        slideVO.setNumShows( slideVO.getNumShows()+1 ) ;
        slideVO.setLastDisplayTime( timestamp ) ;

        Slide slide = slideRepo.findById( slideVO.getId() ).get() ;
        slide.update( slideVO ) ;
        slideRepo.save( slide ) ;

    }

    private void sortAllClusters() {
        // Cluster with the largest average non show delay is given higher
        // precedence to be shown first.
        allClusters.sort( ( c1, c2) -> ( int ) ( c2.getAvgNonShowDelayInMinutes() -
                                                 c1.getAvgNonShowDelayInMinutes() ) ) ;
    }

    private ChapterSlideCluster getCluster( SlideVO s ) {

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
            allClusters.add( chapterCluster ) ;
        }
        return chapterCluster ;
    }
}
