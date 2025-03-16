package com.sandy.sconsole;

import com.sandy.sconsole.core.bus.Payload;
import com.sandy.sconsole.dao.session.dto.ProblemAttemptDTO;
import com.sandy.sconsole.dao.session.dto.SessionDTO;
import com.sandy.sconsole.dao.session.dto.SessionPauseDTO;

public class EventCatalog {

    public static final int CORE_EVENT_RANGE_MIN = 100 ;
    public static final int CORE_EVENT_RANGE_MAX = 200 ;

    // =============== Core Events =============================================
    
    // =============== Session Events ==========================================
    // Range : 201 - 250
    @Payload( SessionDTO.class )
    public static final int SESSION_STARTED = 201 ;
    
    @Payload( SessionPauseDTO.class )
    public static final int PAUSE_STARTED = 202 ;
    
    @Payload( ProblemAttemptDTO.class )
    public static final int PROBLEM_ATTEMPT_STARTED = 203 ;
    
    @Payload( SessionDTO.class )
    public static final int SESSION_EXTENDED = 204 ;
    
    @Payload( SessionPauseDTO.class )
    public static final int PAUSE_EXTENDED = 205 ;
    
    @Payload( ProblemAttemptDTO.class )
    public static final int PROBLEM_ATTEMPT_EXTENDED = 206 ;
    
    @Payload( ProblemAttemptDTO.class )
    public static final int PROBLEM_ATTEMPT_ENDED = 207 ;
    
    // =============== Active Topic Statistics Events ==========================
    // Range : 250 - 300
    
    @Payload( Void.class )
    public static final int ATS_MANAGER_REFRESHED = 250 ;
}
