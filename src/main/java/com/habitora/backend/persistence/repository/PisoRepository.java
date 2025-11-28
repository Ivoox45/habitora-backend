package com.habitora.backend.persistence.repository;

import com.habitora.backend.persistence.entity.Piso;
import com.habitora.backend.persistence.entity.Propiedad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PisoRepository extends JpaRepository<Piso, Long> {

    List<Piso> findByPropiedadOrderByNumeroPisoAsc(Propiedad propiedad);
    
    List<Piso> findByPropiedadId(Long propiedadId);
}
