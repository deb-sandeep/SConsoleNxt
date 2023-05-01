package com.sandy.sconsole.dao.word;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

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
    private float frequency ;

    private boolean hidden = false;
    private boolean starred = false;
    private float rating = 0;
    private int numShows = 0;
    private Timestamp lastDisplayTime = null;
}
