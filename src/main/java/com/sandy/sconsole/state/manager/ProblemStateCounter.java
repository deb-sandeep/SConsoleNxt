package com.sandy.sconsole.state.manager;

import com.sandy.sconsole.dao.master.repo.TopicProblemRepo;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
public class ProblemStateCounter {

    @Getter private int numAssigned = 0 ;
    @Getter private int numCorrect  = 0 ;
    @Getter private int numIncorrect = 0 ;
    @Getter private int numLater = 0 ;
    @Getter private int numPigeons = 0 ;
    @Getter private int numPigeonsExplained = 0 ;
    @Getter private int numPigeonsSolved = 0 ;
    @Getter private int numPurged = 0 ;
    @Getter private int numReassign = 0 ;
    @Getter private int numRedo = 0 ;
    
    @Getter private int totalCount = 0 ;
    
    public ProblemStateCounter() {}
    
    public void populateCounts( List<TopicProblemRepo.ProblemStateCount> stateCounts ) {
        this.resetCountsToZero() ;
        stateCounts.forEach( stateCount -> {
            this.totalCount += stateCount.getNumProblems() ;
            switch( stateCount.getState() ) {
                case "Assigned"         -> this.numAssigned = stateCount.getNumProblems() ;
                case "Correct"          -> this.numCorrect = stateCount.getNumProblems() ;
                case "Incorrect"        -> this.numIncorrect = stateCount.getNumProblems() ;
                case "Later"            -> this.numLater = stateCount.getNumProblems() ;
                case "Pigeon"           -> this.numPigeons = stateCount.getNumProblems() ;
                case "Pigeon Explained" -> this.numPigeonsExplained = stateCount.getNumProblems() ;
                case "Pigeon Solved"    -> this.numPigeonsSolved = stateCount.getNumProblems() ;
                case "Purged"           -> this.numPurged = stateCount.getNumProblems() ;
                case "Reassign"         -> this.numReassign = stateCount.getNumProblems() ;
                case "Redo"             -> this.numRedo = stateCount.getNumProblems() ;
            }
        }) ;
    }
    
    public void resetCountsToZero() {
        this.numAssigned = 0 ;
        this.numCorrect = 0 ;
        this.numIncorrect = 0 ;
        this.numLater = 0 ;
        this.numPigeons = 0 ;
        this.numPigeonsExplained = 0 ;
        this.numPigeonsSolved = 0 ;
        this.numPurged = 0 ;
        this.numReassign = 0 ;
        this.numRedo = 0 ;
        
        this.totalCount = 0 ;
    }
}
