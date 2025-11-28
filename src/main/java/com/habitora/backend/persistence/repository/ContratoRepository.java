package com.habitora.backend.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.habitora.backend.persistence.entity.Contrato;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    @Query("""
            SELECT c FROM Contrato c
            WHERE c.propiedad.id = :propiedadId
            ORDER BY c.fechaInicio DESC
            """)
    List<Contrato> findAllByPropiedad(Long propiedadId);

    @Query("""
            SELECT c FROM Contrato c
            JOIN c.inquilino i
            JOIN c.habitacion h
            WHERE c.propiedad.id = :propiedadId
              AND (:estado IS NULL OR c.estado = :estado)
              AND (
                   :query IS NULL OR :query = ''
                   OR LOWER(i.nombreCompleto) LIKE LOWER(CONCAT(:query, '%'))
                   OR i.numeroDni LIKE CONCAT(:query, '%'))
            ORDER BY c.fechaInicio DESC
            """)
    List<Contrato> searchContratos(Long propiedadId, Contrato.EstadoContrato estado, String query);

    @Query("""
            SELECT COUNT(c) > 0 FROM Contrato c
            WHERE c.habitacion.id = :habitacionId
              AND c.estado = 'ACTIVO'
            """)
    boolean existsActivoInHabitacion(Long habitacionId);

    // Para Dashboard
    List<Contrato> findByPropiedadId(Long propiedadId);
    
    List<Contrato> findByPropiedadIdAndEstado(Long propiedadId, Contrato.EstadoContrato estado);
}
