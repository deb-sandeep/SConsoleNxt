package com.sandy.sconsole.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.List;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class AbstractUtility implements ApplicationContextAware {

    protected static ApplicationContext appCtx = null ;

    protected AbstractUtility() {
    }

    @Override
    public void setApplicationContext( ApplicationContext applicationContext )
            throws BeansException {
        AbstractUtility.appCtx = applicationContext ;
    }

    protected static void closeAppContext() {
        if( AbstractUtility.appCtx != null ) {
            AbstractApplicationContext ac = ( AbstractApplicationContext ) AbstractUtility.appCtx;
            ac.close() ;
        }
    }

    protected List<String> getUtilityResourceFileContents( String utilName, String fileName )
        throws Exception {

        String resPath = "/utility/" + utilName + "/" + fileName ;
        log.debug( "Resource path = {}", resPath ) ;
        return IOUtils.readLines( Objects.requireNonNull( getClass().getResourceAsStream( resPath ) ), UTF_8 ) ;
    }
}
