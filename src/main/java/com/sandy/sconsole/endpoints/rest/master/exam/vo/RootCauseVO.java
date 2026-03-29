package com.sandy.sconsole.endpoints.rest.master.exam.vo;

import com.sandy.sconsole.dao.exam.RootCause;

public record RootCauseVO (
        String cause,
        String group
){
    public static RootCauseVO from( RootCause rc ) {
        return new RootCauseVO( rc.getCause(), rc.getGroup() ) ;
    }
}
