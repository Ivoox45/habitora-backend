package com.habitora.backend.persistence.repository;

import com.habitora.backend.persistence.entity.Propiedad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropiedadRepository extends JpaRepository<Propiedad, Long> {

    List<Propiedad> findByUsuarioId(Long usuarioId);

    Optional<Propiedad> findByIdAndUsuarioId(Long id, Long usuarioId);
}
