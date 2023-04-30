package com.sandy.sconsole.dao.quote;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "quote_master")
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String section = null;
    private String speaker = null;
    private boolean hidden = false;
    private String quote = null;
    private boolean starred = false;
    private float rating = 0;
    private int numShows = 0;
    private Timestamp lastDisplayTime = null;
}
