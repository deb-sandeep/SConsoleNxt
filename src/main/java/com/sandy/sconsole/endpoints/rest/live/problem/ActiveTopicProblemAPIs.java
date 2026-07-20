package com.sandy.sconsole.endpoints.rest.live.problem;

import com.sandy.sconsole.core.api.AR;
import com.sandy.sconsole.core.bus.EventBus;
import com.sandy.sconsole.dao.master.TopicProblem;
import com.sandy.sconsole.dao.master.repo.TopicProblemRepo;
import com.sandy.sconsole.state.manager.DailyBurnLogWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.sandy.sconsole.EventCatalog.BURN_MET_OVERRIDE;
import static com.sandy.sconsole.core.api.AR.success;
import static com.sandy.sconsole.core.api.AR.systemError;

@Slf4j
@RestController
@RequestMapping( "/Topic" )
public class ActiveTopicProblemAPIs {

    @Autowired private TopicProblemRepo tpRepo ;
    @Autowired private DailyBurnLogWriter dailyBurnLogWriter ;
    @Autowired private EventBus eventBus ;

    @GetMapping( "/{topicId}/ActiveProblems" )
    public ResponseEntity<AR<List<TopicProblem>>> getActiveProblems(
                                                @PathVariable int topicId ) {
        try {
            return success( tpRepo.findActiveProblems( topicId )) ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }

    @PostMapping( "/{topicId}/ToggleBurnMetOverride" )
    public ResponseEntity<AR<String>> toggleBurnMetOverride( @PathVariable int topicId ) {
        try {
            dailyBurnLogWriter.toggleBurnMetOverride( topicId ) ;
            eventBus.publishEvent( BURN_MET_OVERRIDE, topicId ) ;
            return success() ;
        }
        catch( Exception e ) {
            return systemError( e ) ;
        }
    }
}
