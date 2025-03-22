package com.sandy.sconsole;

import com.sandy.sconsole.core.bus.Payload;
import com.sandy.sconsole.dao.session.dto.ProblemAttemptDTO;
import com.sandy.sconsole.dao.session.dto.SessionDTO;
import com.sandy.sconsole.dao.session.dto.SessionPauseDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventCatalog {
    
    // =============== Core Events =============================================
    // Range : 100 - 200
    
    // =============== Session Events ==========================================
    // Range : 201 - 250
    @Payload( SessionDTO.class )
    public static final int SESSION_STARTED = 201 ;
    
    @Payload( SessionDTO.class )
    public static final int SESSION_EXTENDED = 202 ;
    
    @Payload( Integer.class ) // Session ID
    public static final int SESSION_ENDED = 203 ;
    
    @Payload( SessionPauseDTO.class )
    public static final int PAUSE_STARTED = 204 ;
    
    @Payload( SessionPauseDTO.class )
    public static final int PAUSE_EXTENDED = 205 ;
    
    @Payload( ProblemAttemptDTO.class )
    public static final int PROBLEM_ATTEMPT_STARTED = 206 ;
    
    @Payload( ProblemAttemptDTO.class )
    public static final int PROBLEM_ATTEMPT_EXTENDED = 207 ;
    
    @Payload( ProblemAttemptDTO.class )
    public static final int PROBLEM_ATTEMPT_ENDED = 208 ;
    
    @Payload( SessionDTO.class )
    public static final int HISTORIC_SESSION_UPDATED = 209 ;
    
    // =============== Active Topic Statistics Events ==========================
    // Range : 250 - 300
    
    @Payload( Void.class )
    public static final int ATS_MANAGER_REFRESHED = 250 ;

    @Payload( Integer.class ) // topic id
    public static final int ATS_REFRESHED = 251 ;

    // =============== Today Study Statistics Events ===========================
    // Range : 301 - 350
    // It is expected that the subscriber of these events will query the
    // TodayStudyStatistics bean for any information that it seeks in
    // response to these events
    @Payload( Void.class )
    public static final int TODAY_STUDY_STATS_UPDATED = 301 ;
    
    @Payload( Void.class )
    public static final int TODAY_STUDY_TIME_UPDATED = 302 ;
    
    // =============== Track Update Events =====================================
    // Range : 351 - 400
    
    @Payload( Integer.class ) // track id
    public static final int TRACK_UPDATED = 351 ;

    // =============== Past Study Time Update Events ===========================
    // Range : 401 - 410
    
    @Payload( Void.class ) // track id
    public static final int PAST_STUDY_TIME_UPDATED = 401 ;

}
