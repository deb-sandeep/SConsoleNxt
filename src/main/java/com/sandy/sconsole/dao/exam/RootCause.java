package com.sandy.sconsole.dao.exam;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table( name = "root_cause_master" )
public class RootCause {
    @Id
    @Size( max = 32 )
    @Column( name = "cause", nullable = false, length = 32 )
    private String cause;
    
    @NotNull
    @Lob
    @Column( name = "`group`", nullable = false )
    private String group;
}
