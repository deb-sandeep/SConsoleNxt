package com.sandy.sconsole.dao.nvp;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NVPConfigDAORepo extends CrudRepository<NVPConfigDAO, Integer> {
    
    public List<NVPConfigDAO> findByGroupName( String groupName ) ;
    
    public List<NVPConfigDAO> findByConfigName( String configName ) ;

    public NVPConfigDAO findByGroupNameAndConfigName( String groupName,
                                                      String configName ) ;
}
