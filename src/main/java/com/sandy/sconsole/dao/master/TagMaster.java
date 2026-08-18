package com.sandy.sconsole.dao.master;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table( name = "tag_master" )
public class TagMaster {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;

    @Column( name = "tag_text", nullable = false, length = 128 )
    private String tagText;

    @Column( name = "normalized_tag_text", nullable = false, length = 128 )
    private String normalizedTagText;

    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "topic_id", nullable = false )
    private Topic topic;

    @Column( name = "created_at", nullable = false )
    private Instant createdAt = Instant.now();
}
