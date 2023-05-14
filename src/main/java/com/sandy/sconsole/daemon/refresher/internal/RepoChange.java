package com.sandy.sconsole.daemon.refresher.internal;

import com.sandy.sconsole.core.util.StringUtil;
import lombok.Getter;

public class RepoChange {

    public enum Type {
        /** Add a new file to the project */
        ADD,
        /** Modify an existing file in the project (content and/or mode) */
        MODIFY,
        /** Delete an existing file from the project */
        DELETE,
        /** Rename an existing file to a new location */
        RENAME,
        /** Copy an existing file to a new location, keeping the original */
        COPY
    }



    @Getter private final Type changeType ;
    @Getter private Path oldPath ;
    @Getter private Path newPath ;

    RepoChange( Type changeType, String oldPath, String newPath ) {
        this.changeType = changeType ;
        if( StringUtil.isNotEmptyOrNull( oldPath ) ) {
            this.oldPath = new Path( oldPath ) ;
        }
        if( StringUtil.isNotEmptyOrNull( newPath ) ) {
            this.newPath = new Path( newPath ) ;
        }
    }

    public String toString() {
        return changeType + " [" + oldPath + "] -> [" + newPath + "]" ;
    }
}
