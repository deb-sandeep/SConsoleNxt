package com.sandy.sconsole.endpoints.websockets.monitor;

import lombok.Data;

@Data
public class AppMonitorResponse {
    
    private String responseType ;
    private Object payload ;
}
