package com.sandy.sconsole.api.master;

import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.master.Syllabus;
import com.sandy.sconsole.dao.master.repo.SyllabusRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping( "/Master/Syllabus" )
public class SyllabusAPIs {
    
    @Autowired
    private SyllabusRepo syllabusRepo = null ;
    
    @GetMapping( "/All" )
    public ResponseEntity<AR<List<Syllabus>>> getAllSyllabus() {
        try {
            List<Syllabus> allSyllabus = new ArrayList<>();
            syllabusRepo.findAll().forEach( allSyllabus::add ) ;
            
            return AR.success( allSyllabus ) ;
        }
        catch( Exception e ) {
            return AR.systemError( e ) ;
        }
    }
}
