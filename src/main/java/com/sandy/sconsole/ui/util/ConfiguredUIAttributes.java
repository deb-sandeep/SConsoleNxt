package com.sandy.sconsole.ui.util;

import com.sandy.sconsole.dao.master.repo.SyllabusRepo;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@Component
public class ConfiguredUIAttributes {
    
    @Autowired private SyllabusRepo syllabusRepo ;
    
    private final Map<String, Color> syllabusColors = new HashMap<>() ;
    
    @PostConstruct
    public void init() {
        syllabusRepo.findAll().forEach( syllabus ->
                syllabusColors.put( syllabus.getSyllabusName(),
                                    Color.decode( syllabus.getColor() ) ) ) ;
    }
    
    public Color getSyllabusColor( String syllabusName ) {
        return syllabusColors.get( syllabusName ) ;
    }
}
