package com.sandy.sconsole.endpoints.web;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Controller
public class SConsoleWebController {
    
    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration               config = new CorsConfiguration();
        
        config.setAllowCredentials( true ) ;
        config.setAllowedOriginPatterns( List.of( "*" ) ) ;
        config.setAllowedMethods( List.of( "*" ) ) ;
        config.setAllowedHeaders( List.of( "*" ) ) ;
        config.setAllowedHeaders( Arrays.asList("Origin", "Content-Type", "Accept") ) ;
        config.setAllowedMethods( Arrays.asList("GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH") ) ;
        config.addExposedHeader( "Content-Disposition" ) ;
        
        source.registerCorsConfiguration( "/**", config ) ;
        return new CorsFilter( source ) ;
    }
}
