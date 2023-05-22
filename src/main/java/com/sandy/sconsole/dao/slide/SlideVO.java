package com.sandy.sconsole.dao.slide;

import lombok.Data;

import java.awt.image.BufferedImage;
import java.sql.Timestamp;

/**
 * A copy of the {@link Slide} class except that this is not a JPA entity.
 * Since we are storing the slide objects, there is a risk of stale entities
 * if we pass the JPA entities around.
 */
@Data
public class SlideVO {

    private Integer id;
    private String syllabus ;
    private String subject ;
    private String chapter ;
    private String slideName ;

    private boolean   hidden          = false;
    private boolean   starred         = false;
    private float     rating          = 0;
    private int       numShows        = 0;
    private Timestamp lastDisplayTime = null;

    private BufferedImage image = null ;

    public String getPath() {
        return syllabus +
                "/" + subject +
                "/" + chapter +
                "/" + slideName ;
    }

    public long getMinutesSinceLastDisplay() {

        long now = System.currentTimeMillis() ;
        long totalNonShowDelayMillis = 0 ;
        
        if( lastDisplayTime == null ) {
            // If a slide has not been displayed ever, assume it was
            // last shown an year back.
            totalNonShowDelayMillis = 31_536_000L * 1000L ;
        }
        else {
            totalNonShowDelayMillis = ( now - lastDisplayTime.getTime() ) ;
        }
        return totalNonShowDelayMillis/60_000 ;
    }

    public SlideVO update( Slide slide ) {
        this.id              = slide.getId() ;
        this.syllabus        = slide.getSyllabus() ;
        this.subject         = slide.getSubject() ;
        this.chapter         = slide.getChapter() ;
        this.slideName       = slide.getSlideName() ;
        this.hidden          = slide.isHidden() ;
        this.starred         = slide.isStarred() ;
        this.rating          = slide.getRating() ;
        this.numShows        = slide.getNumShows() ;
        this.lastDisplayTime = slide.getLastDisplayTime() ;
        return this ;
    }
}
