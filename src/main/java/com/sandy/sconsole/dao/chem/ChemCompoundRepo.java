package com.sandy.sconsole.dao.chem;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChemCompoundRepo extends JpaRepository<ChemCompoundDBO, Integer> {
  
  ChemCompoundDBO findBySmiles( String smiles );
}
