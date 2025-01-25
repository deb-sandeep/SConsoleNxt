package com.sandy.sconsole.api.master.vo.reqres;

import lombok.Data;

@Data
public class SaveBookMetaRes {

    private int numChaptersCreated = 0 ;
    private int numExercisesCreated = 0 ;
    private int numProblemsCreated = 0 ;
    
    public void incChaptersCreated() {
        numChaptersCreated++ ;
    }
    
    public void incExercisesCreated() {
        numExercisesCreated++ ;
    }
    
    public void incProblemsCreated() {
        numProblemsCreated++ ;
    }
}
