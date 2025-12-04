package com.habitora.backend.persistence.repository;

import com.habitora.backend.persistence.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FacturaRepository extends JpaRepository<Factura, Long> {

    // Todas las facturas de una propiedad
    List<Factura> findByContratoPropiedadId(Long propiedadId);

    // Todas las facturas de una propiedad filtradas por estado
    List<Factura> findByContratoPropiedadIdAndEstado(Long propiedadId, Factura.EstadoFactura estado);

    // Facturas de un contrato
    List<Factura> findByContratoId(Long contratoId);

    // Para dashboard - alias para findByContratoPropiedadId
    default List<Factura> findByContratoPropertyId(Long propiedadId) {
        return findByContratoPropiedadId(propiedadId);
    }

    // Para dashboard - facturas con múltiples estados
    default List<Factura> findByContratoPropertyIdAndEstadoIn(Long propiedadId, List<Factura.EstadoFactura> estados) {
        return findByContratoPropiedadId(propiedadId).stream()
                .filter(f -> estados.contains(f.getEstado()))
                .toList();
    }

    /**
     * Busca todas las facturas ABIERTAS (no pagadas, no canceladas)
     * junto con sus relaciones necesarias para enviar recordatorios.
     */
    @Query("SELECT DISTINCT f FROM Factura f " +
           "JOIN FETCH f.contrato c " +
           "JOIN FETCH c.inquilino i " +
           "JOIN FETCH c.habitacion h " +
           "WHERE f.estado = 'ABIERTA' " +
           "ORDER BY f.fechaVencimiento ASC")
    List<Factura> findFacturasAbiertasConDatos();
}
