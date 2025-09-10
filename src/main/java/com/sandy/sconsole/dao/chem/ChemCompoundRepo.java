package com.sandy.sconsole.dao.chem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChemCompoundRepo extends JpaRepository<ChemCompoundDBO, Integer> {
  
  ChemCompoundDBO findBySmiles( String smiles );
    
  List<ChemCompoundDBO> findByCommonNameStartingWithOrderByCommonName( String startSeq );
  
  List<ChemCompoundDBO> findAllByOrderByCommonName() ;
}
