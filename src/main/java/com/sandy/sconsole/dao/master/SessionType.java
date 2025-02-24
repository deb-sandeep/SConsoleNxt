package com.sandy.sconsole.dao.master;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table( name = "session_type_master" )
public class SessionType {
    @Id
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @Column( name = "session_type", nullable = false, length = 64 )
    private String sessionType;
    
    @Column( name = "description", nullable = false, length = 128 )
    private String description;
    
    @Column( name = "color", nullable = false, length = 8 )
    private String color;
    
    @Column( name = "icon_name", nullable = false, length = 45 )
    private String iconName;
}
