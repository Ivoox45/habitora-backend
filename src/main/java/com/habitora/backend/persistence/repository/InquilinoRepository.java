package com.habitora.backend.persistence.repository;

import com.habitora.backend.persistence.entity.Inquilino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InquilinoRepository extends JpaRepository<Inquilino, Long> {

        @Query("""
                        SELECT i FROM Inquilino i
                        WHERE i.propiedad.id = :propiedadId
                        ORDER BY i.nombreCompleto ASC
                        """)
        List<Inquilino> findAllByPropiedad(Long propiedadId);

        @Query("""
                        SELECT i FROM Inquilino i
                        WHERE i.propiedad.id = :propiedadId
                          AND (
                                LOWER(i.nombreCompleto) LIKE LOWER(CONCAT(:query, '%'))
                                OR i.numeroDni LIKE CONCAT(:query, '%')
                              )
                        """)
        List<Inquilino> searchInquilinos(Long propiedadId, String query);

        @Query("""
                        SELECT COUNT(i) > 0
                        FROM Inquilino i
                        WHERE i.propiedad.id = :propiedadId
                          AND i.numeroDni = :dni
                        """)
        boolean existsByPropiedadAndDni(Long propiedadId, String dni);

        @Query("""
                            SELECT i FROM Inquilino i
                            WHERE i.propiedad.id = :propiedadId
                              AND (
                                   LOWER(i.nombreCompleto) LIKE LOWER(CONCAT(:query, '%'))
                                   OR i.numeroDni LIKE CONCAT(:query, '%')
                              )
                            ORDER BY i.nombreCompleto ASC
                        """)
        List<Inquilino> search(Long propiedadId, String query);

        @Query("""
                            SELECT i FROM Inquilino i
                            WHERE i.propiedad.id = :propiedadId
                              AND NOT EXISTS (
                                  SELECT 1 FROM Contrato c
                                  WHERE c.inquilino.id = i.id
                                    AND c.estado = 'ACTIVO'
                              )
                            ORDER BY i.nombreCompleto ASC
                        """)
        List<Inquilino> findDisponibles(Long propiedadId);
}
