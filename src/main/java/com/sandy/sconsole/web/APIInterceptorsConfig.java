package com.sandy.sconsole.web;

import com.sandy.sconsole.core.api.interceptor.APILogInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class APIInterceptorsConfig implements WebMvcConfigurer {
    
    @Autowired
    private APILogInterceptor apiLogInterceptor = null ;
    
    @Override
    public void addInterceptors( InterceptorRegistry registry ) {
        registry.addInterceptor( apiLogInterceptor ) ;
    }
}
