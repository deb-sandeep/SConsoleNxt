package com.sandy.sconsole.dao.master;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table( name = "tag_recent_usage" )
public class TagRecentUsage {

    @Id
    @Column( name = "tag_id", nullable = false )
    private Integer tagId ;

    @Column( name = "last_used_at", nullable = false )
    private Instant lastUsedAt ;
}
