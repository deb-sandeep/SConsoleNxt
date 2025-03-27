package com.sandy.sconsole.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.File;

@Configuration( "config" )
@PropertySource( "classpath:config/sconsole.properties" )
@ConfigurationProperties( "sconsole" )
@Data
public class SConsoleConfig {

    private boolean showSwingApp = true ;
    private String envType = "dev" ;
    private File workspacePath = null ;
    private boolean printEventPublishLogs = false ;
}
