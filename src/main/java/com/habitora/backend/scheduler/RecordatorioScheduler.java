package com.habitora.backend.scheduler;

import com.habitora.backend.persistence.entity.Factura;
import com.habitora.backend.persistence.entity.Recordatorio;
import com.habitora.backend.persistence.repository.FacturaRepository;
import com.habitora.backend.persistence.repository.RecordatorioRepository;
import com.habitora.backend.service.implementation.RecordatorioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Tarea programada para gestionar el envío automático de recordatorios de pago por WhatsApp.
 * Se ejecuta diariamente para:
 * 1. Crear recordatorios para facturas que lo requieran según días al vencimiento
 * 2. Enviar recordatorios programados cuya hora de envío ya llegó
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecordatorioScheduler {

    private final FacturaRepository facturaRepository;
    private final RecordatorioRepository recordatorioRepository;
    private final RecordatorioService recordatorioService;

    /**
     * Tarea que se ejecuta todos los días a las 8:00 AM.
     * Procesa las facturas ABIERTAS y crea recordatorios según corresponda.
     * 
     * Cron expression: "0 0 8 * * ?" significa:
     * - 0 segundos
     * - 0 minutos
     * - 8 horas (8 AM)
     * - * cualquier día del mes
     * - * cualquier mes
     * - ? cualquier día de la semana
     */
    @Scheduled(cron = "0 0 8 * * ?", zone = "America/Lima")
    public void procesarRecordatoriosDiarios() {
        log.info("========================================");
        log.info("🔔 Iniciando procesamiento diario de recordatorios de pago");
        log.info("📅 Fecha: {}", LocalDate.now());
        log.info("========================================");

        try {
            LocalDate fechaActual = LocalDate.now();

            // 1. Obtener todas las facturas ABIERTAS
            List<Factura> facturasAbiertas = facturaRepository.findFacturasAbiertasConDatos();
            log.info("📋 Facturas ABIERTAS encontradas: {}", facturasAbiertas.size());

            if (facturasAbiertas.isEmpty()) {
                log.info("✅ No hay facturas abiertas. Finalizando proceso.");
                return;
            }

            // 2. Procesar cada factura y crear recordatorios si corresponde
            int recordatoriosCreados = 0;
            for (Factura factura : facturasAbiertas) {
                try {
                    boolean creado = recordatorioService.crearRecordatorioSiCorresponde(factura, fechaActual);
                    if (creado) {
                        recordatoriosCreados++;
                    }
                } catch (Exception e) {
                    log.error("❌ Error procesando factura {}: {}", factura.getId(), e.getMessage(), e);
                }
            }

            log.info("✅ Recordatorios creados: {}", recordatoriosCreados);

            // 3. Enviar recordatorios programados cuya hora de envío ya llegó
            enviarRecordatoriosPendientes();

            log.info("========================================");
            log.info("✅ Procesamiento de recordatorios completado");
            log.info("========================================");

        } catch (Exception e) {
            log.error("❌ Error crítico en procesamiento de recordatorios: {}", e.getMessage(), e);
        }
    }

    /**
     * Busca y envía todos los recordatorios que estén programados para ahora o antes.
     * Se ejecuta después de crear los recordatorios del día.
     */
    private void enviarRecordatoriosPendientes() {
        log.info("📤 Enviando recordatorios pendientes...");

        LocalDateTime ahora = LocalDateTime.now();
        List<Recordatorio> recordatoriosPendientes = recordatorioRepository
                .findByEstadoAndProgramadoParaLessThanEqualOrderByProgramadoParaAsc(
                        Recordatorio.EstadoRecordatorio.PROGRAMADO,
                        ahora
                );

        log.info("📨 Recordatorios pendientes de envío: {}", recordatoriosPendientes.size());

        int enviados = 0;
        int fallidos = 0;

        for (Recordatorio recordatorio : recordatoriosPendientes) {
            try {
                recordatorioService.enviarRecordatorio(recordatorio);
                if (recordatorio.getEstado() == Recordatorio.EstadoRecordatorio.ENVIADO) {
                    enviados++;
                } else {
                    fallidos++;
                }
            } catch (Exception e) {
                log.error("❌ Error enviando recordatorio {}: {}", recordatorio.getId(), e.getMessage(), e);
                fallidos++;
            }
        }

        log.info("✅ Recordatorios enviados exitosamente: {}", enviados);
        if (fallidos > 0) {
            log.warn("⚠️ Recordatorios fallidos: {}", fallidos);
        }
    }

    /**
     * Tarea opcional para verificar recordatorios cada hora.
     * Útil si hay recordatorios programados en diferentes horarios.
     * Por defecto está comentado, pero puedes activarlo si lo necesitas.
     */
    // @Scheduled(cron = "0 0 * * * ?", zone = "America/Lima") // Cada hora
    // public void verificarRecordatoriosPendientes() {
    //     log.debug("🔍 Verificación horaria de recordatorios pendientes");
    //     enviarRecordatoriosPendientes();
    // }
}
