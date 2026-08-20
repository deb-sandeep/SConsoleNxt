package com.sandy.sconsole.dao.master;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table( name = "saved_tag_query" )
public class SavedTagQuery {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;

    @Column( name = "name", nullable = false, length = 128 )
    private String name;

    @Lob
    @Column( name = "query", nullable = false )
    private String query;

    @Column( name = "created_at", nullable = false )
    private Instant createdAt = Instant.now();
}
