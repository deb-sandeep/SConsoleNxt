package com.sandy.sconsole.daemon.refresher;

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

    public static class Path {

        @Getter private String syllabus ;
        @Getter private String subject ;
        @Getter private String chapter ;
        @Getter private String fileName ;

        Path( String path ) {
            String[] pathComponents = path.split( "/" ) ;
            if( pathComponents.length >= 1 ) {
                syllabus = pathComponents[0] ;
            }
            if( pathComponents.length >= 2 ) {
                subject = pathComponents[1] ;
            }
            if( pathComponents.length >= 3 ) {
                chapter = pathComponents[2] ;
            }
            if( pathComponents.length >= 4 ) {
                fileName = pathComponents[3] ;
            }
        }

        public boolean isSlide() {
            return fileName != null && fileName.endsWith( ".png" ) ;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder() ;
            sb.append( syllabus ) ;
            if( subject != null ) {
                sb.append( "/" ).append( subject );
                if( chapter != null ) {
                    sb.append( "/" ).append( chapter );
                    if( fileName != null ) {
                        sb.append( "/" ).append( fileName );
                    }
                }
            }
            return sb.toString() ;
        }
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
