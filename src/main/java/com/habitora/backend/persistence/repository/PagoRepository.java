package com.habitora.backend.persistence.repository;

import com.habitora.backend.persistence.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByFacturaId(Long facturaId);

    @Query("""
           SELECT COALESCE(SUM(p.monto), 0)
           FROM Pago p
           WHERE p.factura.id = :facturaId
           """)
    BigDecimal sumarPagosPorFactura(Long facturaId);
}
