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
@Table( name = "problem_type_master" )
public class ProblemType {
    @Id
    @Column( name = "problem_type", nullable = false, length = 8 )
    private String problemType;
}
