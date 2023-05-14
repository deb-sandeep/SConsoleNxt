package com.sandy.sconsole.daemon.refresher;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RefresherGitRepo {

    private final File repoDir ;

    public RefresherGitRepo( File repoDir ) {
        this.repoDir = repoDir ;
    }

    public List<RepoChange> pull() throws Exception {

        List<RepoChange> repoChanges = new ArrayList<>() ;

        try( Git git = Git.open( this.repoDir ) ) {

            ObjectId oldHead, newHead ;

            Repository repository = git.getRepository() ;
            ObjectReader reader = repository.newObjectReader() ;

            // Keep a reference of the HEAD before the pull operation and
            // reacquire the HEAD after the pull operation. It is against
            // these two heads that we will compare the differences to the tree
            // caused by the pull operation.
            oldHead = repository.resolve("HEAD^{tree}") ;
            git.pull().call() ;
            newHead = repository.resolve("HEAD^{tree}") ;

            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser() ;
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser() ;

            oldTreeIter.reset( reader, oldHead ) ;
            newTreeIter.reset( reader, newHead ) ;

            List<DiffEntry> diffs= git.diff()
                                      .setNewTree(newTreeIter)
                                      .setOldTree(oldTreeIter)
                                      .call() ;
            diffs.forEach( diff -> {
                repoChanges.add( buildRepoChange( diff ) ) ;
            } ) ;
        }
        return repoChanges ;
    }

    private RepoChange buildRepoChange( DiffEntry diff ) {
        return new RepoChange( getType( diff.getChangeType() ),
                               diff.getOldPath(),
                               diff.getNewPath() ) ;
    }

    private RepoChange.Type getType( DiffEntry.ChangeType changeType ) {
        return switch( changeType ) {
            case ADD    -> RepoChange.Type.ADD ;
            case COPY   -> RepoChange.Type.COPY ;
            case DELETE -> RepoChange.Type.DELETE ;
            case MODIFY -> RepoChange.Type.MODIFY ;
            case RENAME -> RepoChange.Type.RENAME ;
        } ;
    }
}
