package com.sandy.sconsole.core.api.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class APILogInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle( HttpServletRequest request,
                              HttpServletResponse response,
                              Object handler ) {
        
        log.debug( "API Request :: " + request.getRequestURI() ) ;
        return true;
    }
}
