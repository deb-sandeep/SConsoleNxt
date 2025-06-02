package com.sandy.sconsole.endpoints.rest.live;

import com.sandy.sconsole.core.atomfeed.AtomFeedService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AtomFeedController {
    
    private final AtomFeedService atomFeedService;
    
    public AtomFeedController( AtomFeedService atomFeedService ) {
        this.atomFeedService = atomFeedService;
    }
    
    @GetMapping(value = "/event-feed.xml", produces = "application/atom+xml")
    public void getAtomFeed( HttpServletResponse response) throws Exception {
        response.setContentType("application/atom+xml");
        response.getWriter().write( atomFeedService.getFeed() ) ;
    }
}
