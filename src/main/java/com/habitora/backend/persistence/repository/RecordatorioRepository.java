package com.habitora.backend.persistence.repository;

import com.habitora.backend.persistence.entity.Recordatorio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RecordatorioRepository extends JpaRepository<Recordatorio, Long> {

    /**
     * Busca todos los recordatorios de una factura con estado PROGRAMADO
     * entre un rango de fechas (para evitar duplicados del mismo día).
     */
    List<Recordatorio> findByFacturaIdAndEstadoAndProgramadoParaBetween(
            Long facturaId,
            Recordatorio.EstadoRecordatorio estado,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    /**
     * Obtiene todos los recordatorios programados que aún no han sido enviados
     * y cuya fecha/hora de envío ya ha llegado o pasado.
     */
    List<Recordatorio> findByEstadoAndProgramadoParaLessThanEqualOrderByProgramadoParaAsc(
            Recordatorio.EstadoRecordatorio estado,
            LocalDateTime ahora
    );
}
