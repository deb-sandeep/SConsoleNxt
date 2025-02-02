package com.sandy.sconsole.api.master.vo;

import com.sandy.sconsole.dao.master.Problem;
import lombok.Data;

@Data
public class ProblemVO {
    
    private int id ;
    private Integer exerciseNum ;
    private String exerciseName ;
    private String problemType ;
    private String problemKey ;
    
    public ProblemVO( Problem p ) {
        this.id = p.getId() ;
        this.exerciseNum = p.getExerciseNum() ;
        this.exerciseName = p.getExerciseName() ;
        this.problemType = p.getProblemType().getProblemType() ;
        this.problemKey = p.getProblemKey() ;
    }
}
