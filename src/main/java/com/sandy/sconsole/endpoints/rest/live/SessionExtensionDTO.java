package com.sandy.sconsole.endpoints.rest.live;

import com.sandy.sconsole.dao.session.dto.ProblemAttemptDTO;
import com.sandy.sconsole.dao.session.dto.SessionDTO;
import com.sandy.sconsole.dao.session.dto.SessionPauseDTO;
import lombok.Getter;

public class SessionExtensionDTO {

    @Getter private final SessionDTO        sessionDTO ;
    @Getter private final SessionPauseDTO   pauseDTO ;
    @Getter private final ProblemAttemptDTO problemAttemptDTO ;
    
    public SessionExtensionDTO( SessionDTO session, SessionPauseDTO pause, ProblemAttemptDTO pa ) {
        this.sessionDTO = session ;
        this.pauseDTO = pause ;
        this.problemAttemptDTO = pa ;
    }
}
