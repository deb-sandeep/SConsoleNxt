package com.sandy.sconsole.test.daemon.refresher;

import com.sandy.sconsole.daemon.refresher.internal.Path;
import com.sandy.sconsole.daemon.refresher.internal.RefresherGitRepo;
import com.sandy.sconsole.daemon.refresher.internal.RepoChange;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

@Slf4j
public class RefresherGitRepoTest {

    private static final String REPO_PATH =
            "/home/sandeep/projects/workspace/sconsole/TestRepo" ;

    @Test void pullTest() throws Exception {

        RefresherGitRepo repo = new RefresherGitRepo( new File( REPO_PATH ) ) ;
        List<RepoChange> changes = repo.pull() ;

        for( RepoChange change : changes ) {
            log.debug( change.toString() ) ;
        }
    }

    @Test void resetTest() throws Exception {

        RefresherGitRepo repo = new RefresherGitRepo( new File( REPO_PATH ) ) ;
        List<Path> changes = repo.reset() ;
    }
}
