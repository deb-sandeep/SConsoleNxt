package com.sandy.sconsole.endpoints.rest.live.vo;

import com.sandy.sconsole.dao.session.dto.ProblemAttemptDTO;
import com.sandy.sconsole.dao.session.dto.SessionDTO;
import com.sandy.sconsole.dao.session.dto.SessionPauseDTO;
import lombok.Getter;

public class SessionExtensionVO {

    @Getter private final SessionDTO        sessionDTO ;
    @Getter private final SessionPauseDTO   pauseDTO ;
    @Getter private final ProblemAttemptDTO problemAttemptDTO ;
    
    public SessionExtensionVO( SessionDTO session, SessionPauseDTO pause, ProblemAttemptDTO pa ) {
        this.sessionDTO = session ;
        this.pauseDTO = pause ;
        this.problemAttemptDTO = pa ;
    }
}
