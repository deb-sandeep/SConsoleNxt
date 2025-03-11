package com.sandy.sconsole.dao.word;

import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
@Entity
@Table(name = "word_master")
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String word ;
    private String meaning ;
    private String example ;
    private float  frequency ;

    private boolean   hidden          = false;
    private boolean   starred         = false;
    private boolean   wordnikEnriched = false ;
    private float     rating          = 0;
    private int       numShows        = 0;
    private Timestamp lastDisplayTime = null;

    private int numWordnikTries = 0 ;

    @OneToMany( cascade = CascadeType.ALL,
                mappedBy="word",
                fetch = FetchType.EAGER )
    private List<WordMeaning> meanings = new ArrayList<>() ;

    @OneToMany( cascade = CascadeType.ALL,
                mappedBy="word",
                fetch = FetchType.EAGER )
    private List<WordExample> examples = new ArrayList<>() ;
}
