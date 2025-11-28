package com.habitora.backend.persistence.repository;

import com.habitora.backend.persistence.entity.Habitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {

    List<Habitacion> findByPisoId(Long pisoId);

    long countByPisoId(Long pisoId);

    /**
     * Lista todas las habitaciones de una propiedad,
     * ordenadas por número de piso (piso.numeroPiso ASC).
     */
    List<Habitacion> findByPropiedadIdOrderByPisoNumeroPisoAsc(Long propiedadId);
    
    List<Habitacion> findByPropiedadId(Long propiedadId);
}
