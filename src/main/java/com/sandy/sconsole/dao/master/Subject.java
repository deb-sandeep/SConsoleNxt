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
@Table( name = "subject_master" )
public class Subject {
    @Id
    @Column( name = "subject_name", nullable = false, length = 64 )
    private String subjectName;
}
