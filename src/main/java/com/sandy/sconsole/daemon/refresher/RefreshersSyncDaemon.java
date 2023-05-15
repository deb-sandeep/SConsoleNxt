package com.sandy.sconsole.daemon.refresher;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.SConsoleConfig;
import com.sandy.sconsole.core.behavior.ComponentInitializer;
import com.sandy.sconsole.core.daemon.DaemonBase;
import com.sandy.sconsole.core.nvpconfig.NVPManager;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfig;
import com.sandy.sconsole.daemon.refresher.internal.Path;
import com.sandy.sconsole.daemon.refresher.internal.RefresherGitRepo;
import com.sandy.sconsole.daemon.refresher.internal.RepoChange;
import com.sandy.sconsole.dao.slide.Slide;
import com.sandy.sconsole.dao.slide.SlideRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RefreshersSyncDaemon extends DaemonBase
    implements ComponentInitializer {

    @Autowired
    private SlideRepo slideRepo ;

    @Autowired
    private NVPManager nvpManager ;

    @Autowired
    private SConsoleConfig appCfg ;

    @Autowired
    private RefresherSlideManager slideManager ;

    @NVPConfig private int     startDelaySec = 5 ;
    @NVPConfig private int     runDelayMin = 30 ;
    @NVPConfig private boolean enabled = true ;
    @NVPConfig private boolean resetRepoOnStartup = true ;

    private File repoDir ;

    public RefreshersSyncDaemon(){
        super( "RefreshersSyncDaemon" ) ;
    }

    @Override
    public void initialize( SConsole app ) throws Exception {
        this.repoDir = new File( appCfg.getWorkspacePath(), "Refreshers" ) ;
        super.start() ;
    }

    @Override
    public int getInitializationSequencePreference() {
        return 1000 ;
    }

    public void run() {
        try {
            Thread.sleep( startDelaySec ) ;
        }
        catch( InterruptedException ignore ) {}

        log.debug( "Starting {}.", super.getDaemonName() ) ;
        boolean repoJustResetFlag = false ;
        if( this.resetRepoOnStartup ) {
            try {
                log.debug( "  Resetting repository and syncing slide master." ) ;
                synchronized( RefresherSlideManager.LOCK ) {
                    resetRepoAndSyncSlideMaster();
                    repoJustResetFlag = true;
                }
            }
            catch( Exception e ) {
                log.error( "  Could not reset repo and sync slide master.", e ) ;
                log.error( "  Stopping the daemon" ) ;
                return ;
            }
        }

        while( true ) {
            try {
                if( enabled && !repoJustResetFlag ) {
                    log.debug( "  Pulling repository changes and syncing slide master." ) ;
                    synchronized( RefresherSlideManager.LOCK ) {
                        pullRepoChangesAndSyncSlideMaster();
                    }
                }
            }
            finally {
                try {
                    nvpManager.loadNVPConfigState( this ) ;
                    log.debug( "Daemon run completed. sleeping for {} minutes.", runDelayMin ) ;
                    TimeUnit.MINUTES.sleep( runDelayMin ) ;
                    repoJustResetFlag = false ;
                }
                catch( Exception ignore ){}
            }
        }
    }

    private void resetRepoAndSyncSlideMaster() throws Exception {

        RefresherGitRepo repo = new RefresherGitRepo( this.repoDir ) ;

        List<Path> paths = repo.reset() ;
        List<String> existingSlides = new ArrayList<>() ;

        slideRepo.findAll().forEach( s -> existingSlides.add( s.getPath() ) ) ;

        paths.stream().filter( Path::isSlide ).forEach( p -> {
            if( !existingSlides.remove( p.toString() ) ) {
                log.debug( "   Inserting new slide. {}", p ) ;
                slideRepo.save( p.getNewSlide() ) ;
            }
        } ) ;

        existingSlides.forEach( slidePath -> {
            Slide slide = slideRepo.findByPath( Path.path( slidePath ) ) ;
            if( slide != null ) {
                log.debug( "   Deleting slide. {}", slide.getPath() ) ;
                slideRepo.delete( slide ) ;
            }
        } ) ;
    }

    private void pullRepoChangesAndSyncSlideMaster() {

        try {
            RefresherGitRepo repo = new RefresherGitRepo( this.repoDir ) ;
            List<RepoChange> repoChanges = repo.pull() ;

            repoChanges.forEach( change -> {
                log.debug( "   Applying repo change. {}", change ) ;
                switch( change.getChangeType() ) {
                    case ADD, COPY -> {
                        Slide s = slideRepo.save( change.getNewPath().getNewSlide() ) ;
                        slideManager.add( s.getVO() ) ;
                    }
                    case DELETE -> {
                        Slide s = slideRepo.findByPath( change.getOldPath() ) ;
                        if( s != null ) {
                            slideRepo.delete( s ) ;
                            slideManager.delete( s.getVO() ) ;
                        }
                    }
                    case RENAME -> {
                        Slide oldSlide = slideRepo.findByPath( change.getOldPath() ) ;
                        Slide newSlide = null ;

                        if( oldSlide != null ) {
                            slideManager.delete( oldSlide.getVO() ) ;
                            newSlide = oldSlide.update( change.getNewPath() ) ;
                        }
                        else {
                            newSlide = change.getNewPath().getNewSlide() ;
                        }
                        slideRepo.save( newSlide ) ;
                        slideManager.add( newSlide.getVO() ) ;
                    }
                }
            } ) ;
        }
        catch( Exception e ) {
            log.error( "   Error during pull.", e ) ;
        }
    }
}
