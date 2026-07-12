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

    // Time of day (HH:mm, 24 hour clock) at which the end of day screenshot
    // is captured into ${workspace-path}/eod-screenshots/
    private String eodScreenshotTime = "23:55" ;
    
    private String chemSpiderApiKey = null ;
    private File chemCompoundsImgFolder = null ;
    
    private File questionImgsFolder = null ;
}
