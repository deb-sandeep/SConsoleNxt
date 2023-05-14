package com.sandy.sconsole.daemon.refresher.internal;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RefresherGitRepo {

    private final File repoDir ;

    public RefresherGitRepo( File repoDir ) {
        this.repoDir = repoDir ;
    }

    public List<Path> reset() throws Exception {

        List<Path> allPaths = new ArrayList<>() ;
        try( Git git = Git.open( this.repoDir ) ) {

            Repository repository = git.getRepository() ;

            git.reset()
               .setMode( ResetCommand.ResetType.HARD )
               .call() ;

            TreeWalk treeWalk = new TreeWalk( repository ) ;
            ObjectId head = repository.resolve("HEAD^{tree}") ;
            treeWalk.addTree( head ) ;
            treeWalk.setRecursive( false ) ;
            while( treeWalk.next() ) {
                if( treeWalk.isSubtree() ) {
                    treeWalk.enterSubtree() ;
                }
                else {
                    allPaths.add( new Path( treeWalk.getPathString() ) ) ;
                }
            }
        }
        return allPaths ;
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
            diffs.forEach( d -> repoChanges.add( buildRepoChange( d ) ) ) ;
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
