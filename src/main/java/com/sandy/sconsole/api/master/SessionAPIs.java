package com.sandy.sconsole.api.master;

import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.dao.master.SessionType;
import com.sandy.sconsole.dao.master.repo.SessionTypeRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping( "/Master/Session" )
public class SessionAPIs {
    
    @Autowired private SessionTypeRepo stRepo ;
    
    @GetMapping( "/Types" )
    public ResponseEntity<AR<List<SessionType>>> getAllSessionTypes() {
        try {
            return AR.success( stRepo.findAll() ) ;
        }
        catch( Exception e ) {
            return AR.systemError( e ) ;
        }
    }
}
