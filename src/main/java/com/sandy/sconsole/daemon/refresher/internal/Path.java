package com.sandy.sconsole.daemon.refresher.internal;

import com.sandy.sconsole.dao.slide.Slide;
import lombok.Getter;

public class Path {

    @Getter
    private String syllabus ;
    @Getter private String subject ;
    @Getter private String chapter ;
    @Getter private String fileName ;

    public static Path path( String path ) {
        return new Path( path ) ;
    }

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

    public Slide getNewSlide() {
        Slide slide = new Slide() ;
        slide.setSyllabus ( this.syllabus ) ;
        slide.setSubject  ( this.subject  ) ;
        slide.setChapter  ( this.chapter  ) ;
        slide.setSlideName( this.fileName ) ;
        return slide ;
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