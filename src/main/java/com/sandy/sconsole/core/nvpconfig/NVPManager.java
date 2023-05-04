package com.sandy.sconsole.core.nvpconfig;

import com.sandy.sconsole.SConsole;
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
    
    private NVPConfigDAORepo nvpRepo ;

    // Config group --[*]-> Config name --[*]-> Change listeners
    private final Map<String, Map<String, Set<NVPConfigChangeListener>>> listeners = new HashMap<>() ;

    @Autowired
    public void setNVPConfigDAORepo( NVPConfigDAORepo nvpRepo ) {
        this.nvpRepo = nvpRepo ;
    }
    
    public NVPConfigGroup getConfigGroup( String groupName ) {
        
        NVPConfigGroup group = new NVPConfigGroup( groupName, nvpRepo ) ;
        nvpRepo.findByGroupName( groupName )
                .forEach( nvp -> group.addNVPConfig( new NVPConfig( nvp, nvpRepo ) ) ) ;
        return group ;
    }
    
    public NVPConfig getConfig( String groupName, String keyName,
                                String defaultValue ) {

        NVPConfigDAO nvpDAO = nvpRepo.findByGroupNameAndConfigName( groupName, keyName ) ;
        
        if( nvpDAO == null ) {
            nvpDAO = new NVPConfigDAO( keyName, defaultValue ) ;
            nvpDAO.setGroupName( groupName ) ;
            nvpDAO = nvpRepo.save( nvpDAO ) ;
        }
        return new NVPConfig( nvpDAO, nvpRepo ) ;
    }
    
    public void addConfigChangeListener( NVPConfigChangeListener listener,
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
                              NVPConfigChangeListener listener ) {
        
        Map<String, Set<NVPConfigChangeListener>> keyListenerMap ;
        Set<NVPConfigChangeListener> listenerSet ;

        keyListenerMap = listeners.computeIfAbsent( groupName, k -> new HashMap<>() );
        listenerSet = keyListenerMap.computeIfAbsent( cfgName, k -> new HashSet<>() );

        listenerSet.add( listener ) ;
    }
    
    public void removeConfigChangeListener( NVPConfigChangeListener listener ) {
        
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
        
        Set<NVPConfigChangeListener> listeners = getListeners( nvpConfigDAO ) ;
        NVPConfig cfg = new NVPConfig( nvpConfigDAO, nvpRepo );
        listeners.forEach( listener -> {
            try {
                listener.propertyChanged( cfg ) ;
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
    private Set<NVPConfigChangeListener> getListeners( NVPConfigDAO nvpConfigDAO ) {
        
        Map<String, Set<NVPConfigChangeListener>> keyListenerMap ;
        Set<NVPConfigChangeListener> listenerSet = new HashSet<>() ;
        Set<NVPConfigChangeListener> groupListeners = null ;
        Set<NVPConfigChangeListener> configListeners = null ;

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
