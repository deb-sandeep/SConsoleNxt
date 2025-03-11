package com.sandy.sconsole.endpoints.rest.master.helper;

import com.sandy.sconsole.dao.master.repo.TopicTrackAssignmentRepo;
import com.sandy.sconsole.dao.master.repo.TrackRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TrackHelper {
    
    @Autowired private TrackRepo trackRepo;
    
    @Autowired private TopicTrackAssignmentRepo ttaRepo ;
}
