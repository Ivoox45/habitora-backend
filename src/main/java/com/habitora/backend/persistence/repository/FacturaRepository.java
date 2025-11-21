package com.habitora.backend.persistence.repository;

import com.habitora.backend.persistence.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FacturaRepository extends JpaRepository<Factura, Long> {

    // Todas las facturas de una propiedad
    List<Factura> findByContratoPropiedadId(Long propiedadId);

    // Todas las facturas de una propiedad filtradas por estado
    List<Factura> findByContratoPropiedadIdAndEstado(Long propiedadId, Factura.EstadoFactura estado);

    // Facturas de un contrato
    List<Factura> findByContratoId(Long contratoId);
}
