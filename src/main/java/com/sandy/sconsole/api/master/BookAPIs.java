package com.sandy.sconsole.api.master;

import com.sandy.sconsole.api.master.helper.BookAPIHelper;
import com.sandy.sconsole.api.master.helper.BookMeta;
import com.sandy.sconsole.dao.master.repo.SyllabusRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

import static org.springframework.http.ResponseEntity.status;

@Slf4j
@RestController
@RequestMapping( "/Master/Book" )
public class BookAPIs {
    
    @Autowired
    private SyllabusRepo syllabusRepo = null ;
    
    @Autowired
    private BookAPIHelper helper = null ;
    
    @PostMapping( "/ValidateMetaFile" )
    public ResponseEntity<BookMeta> validateMetaFile(
            @RequestParam( "file" ) MultipartFile multipartFile ) {
        try {
            File savedFile = helper.saveUploadedFile( multipartFile ) ;
            BookMeta meta = helper.parseAndValidateBookMeta( savedFile ) ;
            
            return status( HttpStatus.OK ).body( meta ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting syllabus.", e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR ).body( null );
        }
    }
}
