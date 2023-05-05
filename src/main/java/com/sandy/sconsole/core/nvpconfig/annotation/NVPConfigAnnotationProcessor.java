package com.sandy.sconsole.core.nvpconfig.annotation;

import com.sandy.sconsole.core.nvpconfig.NVPManager;
import com.sandy.sconsole.core.nvpconfig.annotation.internal.NVPConfigTarget;
import com.sandy.sconsole.core.nvpconfig.annotation.internal.NVPConfigTargetCluster;
import com.sandy.sconsole.dao.nvp.NVPConfigDAORepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class NVPConfigAnnotationProcessor {

    private final ApplicationContext appCtx ;

    public NVPConfigAnnotationProcessor( ApplicationContext appCtx ) {
        this.appCtx = appCtx ;
    }

    public void processNVPConfigAnnotations( String... basePackages ) {

        log.debug( "Processing NVPConfig annotations." ) ;
        String[] beanNames = appCtx.getBeanDefinitionNames() ;
        for( String beanName : beanNames ) {
            Object bean = appCtx.getBean( beanName ) ;
            if( isNVPConfigConsumer( bean, basePackages ) ) {
                log.debug( "  Bean - {}", bean.getClass().getName() ) ;
                processNVPConfigConsumer( bean ) ;
            }
        }
    }

    public void persistNVPConfigState( Object obj ) throws IllegalAccessException {
        List<NVPConfigTarget> nvpConfigTargets = extractNVPConfigTargets( obj ) ;
        for( NVPConfigTarget target : nvpConfigTargets ) {
            target.persistState() ;
        }
    }

    private boolean isNVPConfigConsumer( Object bean, String[] basePackages ) {

        Class<?> beanCls = bean.getClass() ;
        if( basePackages != null ) {
            boolean inScope = false ;
            for( String basePackage : basePackages ) {
                if( beanCls.getName().startsWith( basePackage ) ) {
                    inScope = true ;
                    break ;
                }
            }
            if( !inScope ) {
                return false ;
            }
        }

        if( beanCls.isAnnotationPresent( NVPConfigGroup.class ) ) {
            return true ;
        }
        else {
            for( Field f : beanCls.getDeclaredFields() ) {
                if( f.isAnnotationPresent( NVPConfig.class ) ) {
                    return true ;
                }
            }
        }
        return  false ;
    }

    /**
     * The object passed has fields which are marked with @NVPConfig annotations
     * or the class is marked with @NVPConfigGroup annotation.
     */
    private void processNVPConfigConsumer( Object bean ) {

        Map<String, NVPConfigTargetCluster> clusters = new HashMap<>() ;
        List<NVPConfigTarget> targets = extractNVPConfigTargets( bean ) ;

        targets.forEach( t -> {
            NVPConfigTargetCluster tgtCluster ;
            String cfgFQN = t.getFQN() ;

            tgtCluster = clusters.computeIfAbsent( cfgFQN, k ->
                    new NVPConfigTargetCluster( t.getConfigGroupName(),
                                                t.getConfigName() )
            ) ;
            tgtCluster.add( t ) ;
        } ) ;

        NVPManager nvpManager = appCtx.getBean( NVPManager.class ) ;
        clusters.values().forEach( c -> {
            log.debug( "Initializing NVP target cluster {}", c.getFQN() );
            try {
                c.initialize( nvpManager ) ;
            }
            catch( Exception e ) {
                throw new RuntimeException( e );
            }
        } ) ;
    }

    private List<NVPConfigTarget> extractNVPConfigTargets( Object bean ) {

        NVPConfigDAORepo nvpRepo = appCtx.getBean( NVPConfigDAORepo.class ) ;
        List<NVPConfigTarget> targets = new ArrayList<>() ;
        String defaultGroup = getDefaultConfigGroupName( bean.getClass() ) ;
        Field[] fields = bean.getClass().getDeclaredFields() ;

        for( Field field : fields ) {
            if( field.isAnnotationPresent( NVPConfig.class ) ) {
                log.debug( "   Field {}", field.getName() ) ;
                targets.add( new NVPConfigTarget( defaultGroup, field, bean, nvpRepo ) ) ;
            }
        }
        return targets ;
    }

    private String getDefaultConfigGroupName( Class<?> cls ) {
        String retVal = null ;
        if( cls.isAnnotationPresent( NVPConfigGroup.class ) ) {
            retVal = cls.getAnnotation( NVPConfigGroup.class )
                        .groupName() ;
            if( retVal.equals( "" ) ) {
                retVal = cls.getSimpleName() ;
            }
        }
        return retVal ;
    }
}
