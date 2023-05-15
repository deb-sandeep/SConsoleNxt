package com.sandy.sconsole.dao.slide;

import com.sandy.sconsole.daemon.refresher.internal.Path;
import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;

@Slf4j
@Data
@Entity
@Table(name = "slide_master")
public class Slide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public String getPath() {
        return syllabus +
                "/" + subject +
                "/" + chapter +
                "/" + slideName ;
    }

    public Slide update( Path newPath ) {
        this.syllabus  = newPath.getSyllabus() ;
        this.subject   = newPath.getSubject() ;
        this.chapter   = newPath.getChapter() ;
        this.slideName = newPath.getFileName() ;
        return this ;
    }

    public Slide update( SlideVO vo ) {
        this.syllabus        = vo.getSyllabus() ;
        this.subject         = vo.getSubject() ;
        this.chapter         = vo.getChapter() ;
        this.slideName       = vo.getSlideName() ;
        this.hidden          = vo.isHidden() ;
        this.starred         = vo.isStarred() ;
        this.rating          = vo.getRating() ;
        this.numShows        = vo.getNumShows() ;
        this.lastDisplayTime = vo.getLastDisplayTime() ;
        return this ;
    }

    public SlideVO getVO() {
        return new SlideVO().update( this ) ;
    }
}
