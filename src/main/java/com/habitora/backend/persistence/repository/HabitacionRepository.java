package com.habitora.backend.persistence.repository;

import com.habitora.backend.persistence.entity.Habitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {

    List<Habitacion> findByPisoId(Long pisoId);

    long countByPisoId(Long pisoId);
}
