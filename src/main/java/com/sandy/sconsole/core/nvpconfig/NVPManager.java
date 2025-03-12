package com.sandy.sconsole.core.nvpconfig;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.core.nvpconfig.annotation.NVPConfigAnnotationProcessor;
import com.sandy.sconsole.dao.nvp.NVPConfigDAO;
import com.sandy.sconsole.dao.nvp.NVPConfigDAORepo;
import jakarta.persistence.PostUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class NVPManager {
    
    public static class NVPPersistCallback {
        @PostUpdate
        public void postNVPSave( NVPConfigDAO nvpConfigDAO ) {
            SConsole.getAppCtx()
                    .getBean( NVPManager.class )
                    .notifyConfigChangeListeners( nvpConfigDAO ) ;
        }
    }
    
    private @Autowired NVPConfigDAORepo nvpRepo ;
    private @Autowired NVPConfigAnnotationProcessor annotationProcessor ;

    // Config group --[*]-> Config name --[*]-> Change listeners
    private final Map<String, Map<String, Set<NVPCfgChangeListener>>> listeners = new HashMap<>() ;

    @Autowired
    public void setNVPConfigDAORepo( NVPConfigDAORepo nvpRepo ) {
        this.nvpRepo = nvpRepo ;
    }
    
    public NVPCfgGroup getConfigGroup( String groupName ) {
        
        NVPCfgGroup group = new NVPCfgGroup( groupName, nvpRepo ) ;
        nvpRepo.findByGroupName( groupName )
                .forEach( nvp -> group.addNVPConfig( new NVPCfg( nvp, nvpRepo ) ) ) ;
        return group ;
    }

    public NVPCfg getConfig( String groupName, String keyName ) {

        NVPConfigDAO nvpDAO = nvpRepo.findByGroupNameAndConfigName( groupName, keyName ) ;
        if( nvpDAO != null ) {
            return new NVPCfg( nvpDAO, nvpRepo ) ;
        }
        return null ;
    }
    
    public NVPCfg getConfig( String groupName, String keyName,
                             String defaultValue ) {

        NVPConfigDAO nvpDAO = nvpRepo.findByGroupNameAndConfigName( groupName, keyName ) ;
        if( nvpDAO == null ) {
            nvpDAO = new NVPConfigDAO( keyName, defaultValue ) ;
            nvpDAO.setGroupName( groupName ) ;
            nvpDAO = nvpRepo.save( nvpDAO ) ;
        }
        return new NVPCfg( nvpDAO, nvpRepo ) ;
    }

    /**
     * Persists the states of any NVPConfig annotated fields within the object
     * and if any components which are wired to these configs, auto update them.
     * <p/>
     * This convenience method can be used for reverse updates of configuration
     * by setting the attribute values and calling on the NVP manager to
     * persist the changes. Note that just setting the attribute values
     * does not trigger persistence.
     */
    public void persistNVPConfigState( Object obj ) throws IllegalAccessException {
        annotationProcessor.persistNVPConfigState( obj ) ;
    }

    /**
     * If there are any @NVPConfig annotated fields in the given object,
     * they will be populated.
     */
    public void loadNVPConfigState( Object obj ) throws Exception {
        annotationProcessor.loadNVPConfigState( obj ) ;
    }
    
    public void addConfigChangeListener( NVPCfgChangeListener listener,
                                         String groupName, 
                                         String... cfgKeys ) {
        
        if( cfgKeys != null && cfgKeys.length > 0 ) {
            for( String cfgKey : cfgKeys ) {
                addListener( groupName, cfgKey, listener ) ;
            }
        }
        else {
            // If config keys are not provided, it implies that the listener
            // is interested in any config change at the group level.
            addListener( groupName, "*", listener ) ;
        }
    }
    
    private void addListener( String groupName, String cfgName,
                              NVPCfgChangeListener listener ) {
        
        Map<String, Set<NVPCfgChangeListener>> keyListenerMap ;
        Set<NVPCfgChangeListener>              listenerSet ;

        keyListenerMap = listeners.computeIfAbsent( groupName, k -> new HashMap<>() );
        listenerSet = keyListenerMap.computeIfAbsent( cfgName, k -> new HashSet<>() );

        listenerSet.add( listener ) ;
    }
    
    public void removeConfigChangeListener( NVPCfgChangeListener listener ) {
        
        listeners.values().forEach( keyListenerMap ->
                keyListenerMap.values().forEach( listenerSet ->
                        listenerSet.remove( listener )
                )
        ) ;
    }

    public  void removeAllConfigChangeListeners() {
        listeners.clear() ;
    }
    
    private void notifyConfigChangeListeners( NVPConfigDAO nvpConfigDAO ) {
        
        Set<NVPCfgChangeListener> listeners = getListeners( nvpConfigDAO ) ;
        NVPCfg                    cfg       = new NVPCfg( nvpConfigDAO, nvpRepo );
        listeners.forEach( listener -> {
            try {
                listener.nvpConfigChanged( cfg ) ;
            }
            catch( Exception e ) {
                log.error( "Config change listener error", e ) ;
            }
        } ) ;
    }

    /**
     * Returns a set of all listeners who are interested in changes to this
     * configuration.
     *
     * @param nvpConfigDAO The config who change listeners we are interested in.
     *
     * @return A set of listeners. The set includes the union of listeners who
     *      are explicitly registered for listening to this config, and any
     *      listeners who are registered at group level. Guaranteed to be not null.
     */
    private Set<NVPCfgChangeListener> getListeners( NVPConfigDAO nvpConfigDAO ) {
        
        Map<String, Set<NVPCfgChangeListener>> keyListenerMap ;
        Set<NVPCfgChangeListener>              listenerSet = new HashSet<>() ;
        Set<NVPCfgChangeListener>              groupListeners ;
        Set<NVPCfgChangeListener>              configListeners ;

        keyListenerMap = listeners.get( nvpConfigDAO.getGroupName() ) ;
        if( keyListenerMap != null ) {

            configListeners = keyListenerMap.get( nvpConfigDAO.getConfigName() ) ;
            groupListeners = keyListenerMap.get( "*" ) ;

            if( groupListeners != null ) {
                listenerSet.addAll( groupListeners ) ;
            }

            if( configListeners != null ) {
                listenerSet.addAll( configListeners ) ;
            }
        }
        return listenerSet ;
    }
}
