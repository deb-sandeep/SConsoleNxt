package com.sandy.sconsole.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration( "config" )
@PropertySource( "classpath:sconsole.properties" )
@ConfigurationProperties( "sconsole" )
@Data
public class SConsoleConfig {

    private boolean showSwingApp = true ;
    private String envType = "dev" ;
}
