package com.sandy.sconsole.core.nvpconfig.annotation;

import com.sandy.sconsole.core.nvpconfig.NVPManager;
import com.sandy.sconsole.core.nvpconfig.annotation.internal.NVPConfigTarget;
import com.sandy.sconsole.core.nvpconfig.annotation.internal.NVPCfgTargetCluster;
import com.sandy.sconsole.core.util.StringUtil;
import com.sandy.sconsole.dao.nvp.NVPConfigDAORepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
@Component
public class NVPConfigAnnotationProcessor implements ApplicationContextAware {

    private ApplicationContext appCtx ;

    public NVPConfigAnnotationProcessor() {}
    
    @Override
    public void setApplicationContext( ApplicationContext applicationContext )
            throws BeansException {
        this.appCtx = applicationContext ;
    }

    public void processNVPConfigAnnotations( String... basePackages ) {

        log.debug( "> Processing NVPConfig annotations." ) ;
        String[] beanNames = appCtx.getBeanDefinitionNames() ;
        for( String beanName : beanNames ) {
            Object bean = appCtx.getBean( beanName ) ;
            if( isNVPConfigConsumer( bean, basePackages ) ) {
                log.debug( "-> NVPConsumer {} found.", bean.getClass().getSimpleName() ) ;
                processNVPConfigConsumer( bean ) ;
            }
        }
        log.debug( "- NVPConfig annotations processed. <" ) ;
    }

    public void persistNVPConfigState( Object obj ) throws IllegalAccessException {
        List<NVPConfigTarget> nvpConfigTargets = extractNVPConfigTargets( obj ) ;
        for( NVPConfigTarget target : nvpConfigTargets ) {
            target.persistState() ;
        }
    }

    public void loadNVPConfigState( Object obj ) throws InvocationTargetException, IllegalAccessException {
        List<NVPConfigTarget> nvpConfigTargets = extractNVPConfigTargets( obj ) ;
        for( NVPConfigTarget target : nvpConfigTargets ) {
            target.loadState() ;
        }
    }

    private boolean isNVPConfigConsumer( Object bean, String[] basePackages ) {

        Class<?> beanCls = bean.getClass() ;
        if( basePackages != null && basePackages.length > 0 ) {
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
     * <p/>
     * Note that non-component instances can be registered with the annotation
     * processor directly using this method. However, this method should be
     * used with care since it can cause memory leaks since the object will be
     * stored and there is no way to remove this object simply.
     */
    public void processNVPConfigConsumer( Object bean ) {

        Map<String, NVPCfgTargetCluster> clusters = new HashMap<>() ;
        List<NVPConfigTarget>            targets  = extractNVPConfigTargets( bean ) ;

        if( targets.isEmpty() ) {
            log.debug( "- Bean does not have any NVPConfig targets." ) ;
            return ;
        }

        targets.forEach( t -> {
            NVPCfgTargetCluster tgtCluster ;
            String              cfgFQN = t.getFQN() ;

            tgtCluster = clusters.computeIfAbsent( cfgFQN, k ->
                    new NVPCfgTargetCluster( t.getConfigGroupName(),
                                                t.getConfigName() )
            ) ;
            tgtCluster.add( t ) ;
        } ) ;

        NVPManager nvpManager = appCtx.getBean( NVPManager.class ) ;
        clusters.values().forEach( c -> {
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
        List<Field> fields = getAllFields( bean.getClass() ) ;
        Method updateMethod = getUpdateMethodIfAny( bean.getClass() ) ;

        for( Field field : fields ) {
            NVPConfigTarget target ;
            if( field.isAnnotationPresent( NVPConfig.class ) ) {
                target = new NVPConfigTarget( defaultGroup, field,
                                              updateMethod, bean, nvpRepo ) ;
                log.debug( "->> NVP Field - {} -> {}.{}", field.getName(),
                                                    target.getConfigGroupName(),
                                                    target.getConfigName() ) ;
                targets.add( target ) ;
            }
        }
        return targets ;
    }

    private List<Field> getAllFields( Class<?> type ) {
        List<Field> fields = new ArrayList<>() ;
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll( Arrays.asList(c.getDeclaredFields())) ;
        }
        return fields;
    }

    private Method getUpdateMethodIfAny( Class<?> aClass ) {

        for( Method method : aClass.getDeclaredMethods() ) {
            if( method.isAnnotationPresent( NVPConfigChangeListener.class ) ) {
                return method ;
            }
        }
        return null ;
    }

    private String getDefaultConfigGroupName( Class<?> cls ) {
        String retVal = null ;
        if( cls.isAnnotationPresent( NVPConfigGroup.class ) ) {
            retVal = cls.getAnnotation( NVPConfigGroup.class )
                        .groupName() ;
        }
        if( StringUtil.isEmptyOrNull( retVal ) ) {
            retVal = cls.getSimpleName() ;
        }
        return retVal ;
    }

}
