package com.sandy.sconsole.dao.word;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "word_example")
public class WordExample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn( name="word_id" )
    private Word word ;

    private String example ;
}
