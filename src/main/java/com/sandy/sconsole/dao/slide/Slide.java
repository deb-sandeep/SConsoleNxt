package com.sandy.sconsole.dao.slide;

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
    private boolean   wordnikEnriched = false ;
    private float     rating          = 0;
    private int       numShows        = 0;
    private Timestamp lastDisplayTime = null;
}
