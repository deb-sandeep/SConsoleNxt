package com.sandy.sconsole.endpoints.web;

import com.sandy.sconsole.core.SConsoleConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired private SConsoleConfig config ;
    
    @Override
    public void addResourceHandlers( ResourceHandlerRegistry registry) {
        registry.addResourceHandler( "/chem-compound-img/**" ) // URL path
                .addResourceLocations( "file:" + config.getChemCompoundsImgFolder().getAbsolutePath() ) ;
    }
}
