package com.sandy.sconsole.core.nvpconfig;

import com.sandy.sconsole.SConsole;
import com.sandy.sconsole.dao.nvp.NVPConfigDAO;
import com.sandy.sconsole.dao.nvp.NVPConfigDAORepo;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class NVPManager {
    
    public static class NVPPersistCallback {

        @PostPersist
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
            nvpRepo.findByGroupName( groupName )
                    .forEach( nvpDAO -> addListener( groupName,
                                                     nvpDAO.getConfigName(),
                                                     listener ) );
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
    
    private void notifyConfigChangeListeners( NVPConfigDAO nvpConfigDAO ) {
        
        Set<NVPConfigChangeListener> listeners = getListeners( nvpConfigDAO ) ;
        if( listeners != null ) {
            NVPConfig cfg = new NVPConfig( nvpConfigDAO, nvpRepo ) ;
            listeners.forEach( listener -> {
                try {
                    listener.propertyChanged( cfg ) ;
                }
                catch( Exception e ) {
                    log.error( "Config change listener error", e ) ;
                }
            } ) ;
        }
    }
    
    private Set<NVPConfigChangeListener> getListeners( NVPConfigDAO nvpConfigDAO ) {
        
        Map<String, Set<NVPConfigChangeListener>> keyListenerMap ;
        Set<NVPConfigChangeListener> listenerSet = null ;

        keyListenerMap = listeners.get( nvpConfigDAO.getGroupName() ) ;
        if( keyListenerMap != null ) {
            listenerSet = keyListenerMap.get( nvpConfigDAO.getConfigName() ) ;
        }
        return listenerSet ;
    }
}
