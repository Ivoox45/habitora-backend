package com.habitora.backend.service.implementation;

import com.habitora.backend.integration.whatsapp.WhatsAppService;
import com.habitora.backend.persistence.entity.Factura;
import com.habitora.backend.persistence.entity.Recordatorio;
import com.habitora.backend.persistence.repository.RecordatorioRepository;
import com.habitora.backend.persistence.repository.FacturaRepository;
import com.habitora.backend.presentation.dto.recordatorio.EstadisticasRecordatoriosDto;
import com.habitora.backend.presentation.dto.recordatorio.RecordatorioDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar recordatorios de pago automáticos.
 * Crea y envía recordatorios por WhatsApp según la proximidad a la fecha de vencimiento.
 */
@Slf4j
@Service
@Transactional
public class RecordatorioService {

    private final RecordatorioRepository recordatorioRepository;
    private final WhatsAppService whatsAppService;
    private final FacturaRepository facturaRepository;
    private final com.habitora.backend.persistence.repository.ConfigRecordatorioRepository configRecordatorioRepository;

    public RecordatorioService(
            RecordatorioRepository recordatorioRepository,
            @Autowired(required = false) WhatsAppService whatsAppService,
            FacturaRepository facturaRepository,
            com.habitora.backend.persistence.repository.ConfigRecordatorioRepository configRecordatorioRepository) {
        this.recordatorioRepository = recordatorioRepository;
        this.whatsAppService = whatsAppService;
        this.facturaRepository = facturaRepository;
        this.configRecordatorioRepository = configRecordatorioRepository;
    }

    /**
     * Crea un recordatorio para una factura si corresponde según los días restantes
     * hasta el vencimiento (-3, -2, -1, 0, +1, +2).
     *
     * @param factura Factura para la cual crear el recordatorio
     * @param fechaActual Fecha actual para calcular días restantes
     * @return true si se creó el recordatorio, false si no correspondía o ya existía
     */
    public boolean crearRecordatorioSiCorresponde(Factura factura, LocalDate fechaActual) {
                // Verificar configuración por propiedad: si está desactivada, no crear
                Long propiedadId = factura.getContrato().getPropiedad().getId();
                boolean activo = configRecordatorioRepository.findByPropiedadId(propiedadId)
                                .map(com.habitora.backend.persistence.entity.ConfigRecordatorio::getEstaActivo)
                                .orElse(true);
                if (!activo) {
                        return false;
                }
        // Calcular días restantes hasta el vencimiento
        long diasRestantes = ChronoUnit.DAYS.between(fechaActual, factura.getFechaVencimiento());
        int dias = (int) diasRestantes;

                // Verificar si corresponde enviar recordatorio para este día usando offsets de configuración
                List<Integer> offsetsConfig = configRecordatorioRepository.findByPropiedadId(propiedadId)
                                .map(c -> {
                                        String csv = c.getOffsetsCsv();
                                        if (csv == null || csv.isBlank()) return List.of(-3,-2,-1,0,1,2);
                                        try {
                                                return java.util.Arrays.stream(csv.split(","))
                                                                .map(String::trim)
                                                                .filter(s -> !s.isEmpty())
                                                                .map(Integer::parseInt)
                                                                .toList();
                                        } catch (Exception e) {
                                                return List.of(-3,-2,-1,0,1,2);
                                        }
                                })
                                .orElse(List.of(-3,-2,-1,0,1,2));
                if (!offsetsConfig.contains(dias)) {
                        return false;
                }

        // Verificar que el inquilino tenga teléfono registrado
        String telefonoLocal = factura.getContrato().getInquilino().getTelefonoWhatsapp();
        if (telefonoLocal == null || telefonoLocal.isBlank()) {
            log.warn("⚠️ Inquilino {} no tiene teléfono registrado. No se puede enviar recordatorio.",
                    factura.getContrato().getInquilino().getNombreCompleto());
            return false;
        }

        // Verificar que no exista ya un recordatorio para este día
        // Hora de envío: usa configuración si existe, sino 08:00
        java.time.LocalTime horaEnvio = configRecordatorioRepository.findByPropiedadId(propiedadId)
                .map(com.habitora.backend.persistence.entity.ConfigRecordatorio::getHoraEnvio)
                .orElse(LocalTime.of(8, 0));
        LocalDateTime programadoPara = LocalDateTime.of(fechaActual, horaEnvio);
        LocalDateTime inicioDia = fechaActual.atStartOfDay();
        LocalDateTime finDia = fechaActual.atTime(23, 59, 59);
        
        List<Recordatorio> recordatoriosExistentes = recordatorioRepository
                .findByFacturaIdAndEstadoAndProgramadoParaBetween(
                        factura.getId(),
                        Recordatorio.EstadoRecordatorio.PROGRAMADO,
                        inicioDia,
                        finDia
                );
        
        if (!recordatoriosExistentes.isEmpty()) {
            log.debug("Ya existe recordatorio para factura {} en fecha {}", factura.getId(), fechaActual);
            return false;
        }

        // Generar mensaje personalizado (prioriza template configuración si existe)
        String mensajeConfig = configRecordatorioRepository.findByPropiedadId(propiedadId)
                .map(com.habitora.backend.persistence.entity.ConfigRecordatorio::getMensajeTemplate)
                .orElse(null);
        String mensaje = (mensajeConfig != null && !mensajeConfig.isBlank())
                ? construirMensajeDesdePlantilla(mensajeConfig,
                    factura.getContrato().getInquilino().getNombreCompleto(),
                    factura.getMontoRenta(),
                    factura.getContrato().getHabitacion().getCodigo(),
                    dias)
                : MensajeRecordatorioTemplate.generarMensaje(
                dias,
                factura.getContrato().getInquilino().getNombreCompleto(),
                factura.getMontoRenta(),
                factura.getContrato().getHabitacion().getCodigo()
        );

        if (mensaje == null) {
            log.warn("No se pudo generar mensaje para {} días restantes", dias);
            return false;
        }

        // Formatear teléfono a formato internacional
        String telefonoInternacional = (whatsAppService != null) 
            ? whatsAppService.formatearNumeroPeruano(telefonoLocal)
            : "+51" + telefonoLocal;

        // Crear y guardar el recordatorio
        Recordatorio recordatorio = Recordatorio.builder()
                .factura(factura)
                .contrato(factura.getContrato())
                .programadoPara(programadoPara)
                .canal(Recordatorio.Canal.WHATSAPP)
                .telefonoDestino(telefonoInternacional)
                .mensaje(mensaje)
                .estado(Recordatorio.EstadoRecordatorio.PROGRAMADO)
                .build();

        recordatorioRepository.save(recordatorio);

        log.info("✅ Recordatorio creado para factura {} | Inquilino: {} | Días restantes: {}",
                factura.getId(),
                factura.getContrato().getInquilino().getNombreCompleto(),
                dias);

        return true;
    }

        private String construirMensajeDesdePlantilla(String plantilla, String nombre, java.math.BigDecimal monto, String habitacion, int dias) {
                String msg = plantilla;
                msg = msg.replace("{nombre}", nombre);
                msg = msg.replace("{monto}", monto.toString());
                msg = msg.replace("{habitacion}", habitacion);
                msg = msg.replace("{dias}", String.valueOf(dias));
                return msg;
        }

    /**
     * Envía un recordatorio por WhatsApp y actualiza su estado.
     *
     * @param recordatorio Recordatorio a enviar
     */
    public void enviarRecordatorio(Recordatorio recordatorio) {
        try {
            log.info("Enviando recordatorio {} a {}", recordatorio.getId(), recordatorio.getTelefonoDestino());

            if (whatsAppService == null) {
                log.warn("WhatsApp no está configurado. Marcando recordatorio como fallido.");
                recordatorio.setEstado(Recordatorio.EstadoRecordatorio.FALLIDO);
                recordatorioRepository.save(recordatorio);
                return;
            }

            String messageId = whatsAppService.enviarMensaje(
                    recordatorio.getTelefonoDestino(),
                    recordatorio.getMensaje()
            );

            if (messageId != null) {
                // Envío exitoso
                recordatorio.setEstado(Recordatorio.EstadoRecordatorio.ENVIADO);
                recordatorio.setEnviadoEn(LocalDateTime.now());
                recordatorio.setIdMensajeProveedor(messageId);
                log.info("✅ Recordatorio {} enviado exitosamente. Message ID: {}", recordatorio.getId(), messageId);
            } else {
                // Fallo en el envío
                recordatorio.setEstado(Recordatorio.EstadoRecordatorio.FALLIDO);
                log.error("❌ Fallo al enviar recordatorio {}", recordatorio.getId());
            }

            recordatorioRepository.save(recordatorio);

        } catch (Exception e) {
            log.error("❌ Error al enviar recordatorio {}: {}", recordatorio.getId(), e.getMessage(), e);
            recordatorio.setEstado(Recordatorio.EstadoRecordatorio.FALLIDO);
            recordatorioRepository.save(recordatorio);
        }
    }


        /**
         * Procesa las facturas abiertas y crea recordatorios para el día actual.
         * Útil para pruebas manuales desde el panel.
         */
        public int procesarRecordatoriosDeHoy() {
                LocalDate hoy = LocalDate.now();
                List<Factura> facturas = facturaRepository.findFacturasAbiertasConDatos();
                int creados = 0;
                for (Factura f : facturas) {
                        if (crearRecordatorioSiCorresponde(f, hoy)) {
                                creados++;
                        }
                }
                return creados;
        }

        /**
         * Crea recordatorios manuales para múltiples inquilinos según un patrón.
         */
        public int crearRecordatoriosManuales(Long propiedadId, List<Long> inquilinoIds, String patron, String mensajePersonalizado,
                                                                                  LocalTime horaEnvio, LocalDate fechaPersonalizada, Long usuarioId) {
                // Obtener todas las facturas abiertas con datos (optimizamos reutilizando consulta existente)
                List<Factura> facturas = facturaRepository.findFacturasAbiertasConDatos();

                // Filtrar por propiedad y inquilinos seleccionados y contrato ACTIVO
                facturas = facturas.stream()
                                .filter(f -> f.getContrato().getPropiedad().getId().equals(propiedadId))
                                .filter(f -> f.getContrato().getEstado() == com.habitora.backend.persistence.entity.Contrato.EstadoContrato.ACTIVO)
                                .filter(f -> inquilinoIds.contains(f.getContrato().getInquilino().getId()))
                                .toList();

                int creados = 0;
                for (Factura factura : facturas) {
                        List<Integer> diasOffsets = obtenerOffsetsPatron(patron);

                        for (Integer offset : diasOffsets) {
                                LocalDate fechaBase = factura.getFechaVencimiento().plusDays(offset);
                                LocalDateTime programadoPara;
                                if ("PERSONALIZADO".equals(patron) && fechaPersonalizada != null) {
                                        programadoPara = LocalDateTime.of(fechaPersonalizada, horaEnvio != null ? horaEnvio : LocalTime.of(8,0));
                                } else {
                                        programadoPara = LocalDateTime.of(fechaBase, horaEnvio != null ? horaEnvio : LocalTime.of(8,0));
                                }

                                // Evitar duplicados (mismo contrato + misma fecha programada + PROGRAMADO)
                                boolean existe = recordatorioRepository.findByFacturaIdAndEstadoAndProgramadoParaBetween(
                                                factura.getId(),
                                                Recordatorio.EstadoRecordatorio.PROGRAMADO,
                                                programadoPara.minusMinutes(1),
                                                programadoPara.plusMinutes(1)
                                ).stream().anyMatch(r -> r.getContrato().getId().equals(factura.getContrato().getId()));
                                if (existe) continue;

                                String mensaje = construirMensajeManual(factura, mensajePersonalizado, offset);

                                String telefono = factura.getContrato().getInquilino().getTelefonoWhatsapp();
                                String telefonoFormateado = (whatsAppService != null)
                                    ? whatsAppService.formatearNumeroPeruano(telefono)
                                    : "+51" + telefono;

                                Recordatorio nuevo = Recordatorio.builder()
                                                .factura(factura)
                                                .contrato(factura.getContrato())
                                                .programadoPara(programadoPara)
                                                .canal(Recordatorio.Canal.WHATSAPP)
                                                .telefonoDestino(telefonoFormateado)
                                                .mensaje(mensaje)
                                                .estado(Recordatorio.EstadoRecordatorio.PROGRAMADO)
                                                .tipo(Recordatorio.TipoRecordatorio.MANUAL)
                                                .creadoPorUsuarioId(usuarioId)
                                                .build();
                                recordatorioRepository.save(nuevo);
                                creados++;
                        }
                }
                return creados;
        }

        private List<Integer> obtenerOffsetsPatron(String patron) {
                return switch (patron) {
                        case "COMPLETO" -> List.of(-3,-2,-1,0,1,2);
                        case "SOLO_ANTES" -> List.of(-3,-2,-1);
                        case "SOLO_VENCIMIENTO" -> List.of(0);
                        case "SOLO_DESPUES" -> List.of(1,2);
                        case "PERSONALIZADO" -> List.of(0); // Se usa fecha personalizada real en este caso
                        default -> List.of();
                };
        }

        private String construirMensajeManual(Factura factura, String plantilla, int offsetDias) {
                if (plantilla == null || plantilla.isBlank()) {
                        // fallback a template existente
                        return MensajeRecordatorioTemplate.generarMensaje(
                                        offsetDias,
                                        factura.getContrato().getInquilino().getNombreCompleto(),
                                        factura.getMontoRenta(),
                                        factura.getContrato().getHabitacion().getCodigo()
                        );
                }
                String msg = plantilla;
                msg = msg.replace("{nombre}", factura.getContrato().getInquilino().getNombreCompleto());
                msg = msg.replace("{monto}", factura.getMontoRenta().toString());
                msg = msg.replace("{habitacion}", factura.getContrato().getHabitacion().getCodigo());
                msg = msg.replace("{dias}", String.valueOf(offsetDias));
                return msg;
        }

    /**
     * Lista todos los recordatorios de una propiedad con filtros opcionales.
     */
    @Transactional(readOnly = true)
    public List<RecordatorioDto> listarRecordatorios(
            Long propiedadId,
            String estado,
            String tipo,
            String fechaDesde,
            String fechaHasta,
            String inquilinoNombre) {
        
        List<Recordatorio> recordatorios = recordatorioRepository.findAll();
        
        // Filtrar por propiedad (a través del contrato)
        recordatorios = recordatorios.stream()
                .filter(r -> r.getContrato().getHabitacion().getPiso().getPropiedad().getId().equals(propiedadId))
                .collect(Collectors.toList());
        
        // Filtrar por estado si se proporciona
        if (estado != null && !estado.isBlank()) {
            Recordatorio.EstadoRecordatorio estadoEnum = Recordatorio.EstadoRecordatorio.valueOf(estado);
            recordatorios = recordatorios.stream()
                    .filter(r -> r.getEstado().equals(estadoEnum))
                    .collect(Collectors.toList());
        }

        // Filtrar por tipo si se proporciona
        if (tipo != null && !tipo.isBlank()) {
            Recordatorio.TipoRecordatorio tipoEnum = Recordatorio.TipoRecordatorio.valueOf(tipo);
            recordatorios = recordatorios.stream()
                    .filter(r -> r.getTipo().equals(tipoEnum))
                    .collect(Collectors.toList());
        }
        
        // Filtrar por fecha desde
        if (fechaDesde != null && !fechaDesde.isBlank()) {
            LocalDate desde = LocalDate.parse(fechaDesde);
            recordatorios = recordatorios.stream()
                    .filter(r -> !r.getProgramadoPara().toLocalDate().isBefore(desde))
                    .collect(Collectors.toList());
        }
        
        // Filtrar por fecha hasta
        if (fechaHasta != null && !fechaHasta.isBlank()) {
            LocalDate hasta = LocalDate.parse(fechaHasta);
            recordatorios = recordatorios.stream()
                    .filter(r -> !r.getProgramadoPara().toLocalDate().isAfter(hasta))
                    .collect(Collectors.toList());
        }
        
        // Filtrar por nombre de inquilino
        if (inquilinoNombre != null && !inquilinoNombre.isBlank()) {
            String nombreLower = inquilinoNombre.toLowerCase();
            recordatorios = recordatorios.stream()
                    .filter(r -> r.getContrato().getInquilino().getNombreCompleto().toLowerCase().contains(nombreLower))
                    .collect(Collectors.toList());
        }
        
        // Convertir a DTO
        return recordatorios.stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

        /**
         * Obtiene un recordatorio por id validando que pertenezca a la propiedad.
         */
        @Transactional(readOnly = true)
        public RecordatorioDto obtenerRecordatorioPorId(Long propiedadId, Long recordatorioId) {
                Recordatorio r = recordatorioRepository.findById(recordatorioId)
                                .orElseThrow(() -> new RuntimeException("Recordatorio no encontrado"));

                Long propId = r.getContrato().getHabitacion().getPiso().getPropiedad().getId();
                if (!propId.equals(propiedadId)) {
                        throw new RuntimeException("Recordatorio no pertenece a la propiedad indicada");
                }

                return convertirADto(r);
        }

    /**
     * Obtiene las estadísticas de recordatorios de una propiedad.
     */
    @Transactional(readOnly = true)
    public EstadisticasRecordatoriosDto obtenerEstadisticas(Long propiedadId) {
        List<Recordatorio> todosRecordatorios = recordatorioRepository.findAll();
        
        // Filtrar por propiedad
        List<Recordatorio> recordatoriosPropiedad = todosRecordatorios.stream()
                .filter(r -> r.getContrato().getHabitacion().getPiso().getPropiedad().getId().equals(propiedadId))
                .collect(Collectors.toList());
        
        long totalProgramados = recordatoriosPropiedad.stream()
                .filter(r -> r.getEstado() == Recordatorio.EstadoRecordatorio.PROGRAMADO)
                .count();
        
        long totalEnviados = recordatoriosPropiedad.stream()
                .filter(r -> r.getEstado() == Recordatorio.EstadoRecordatorio.ENVIADO)
                .count();
        
        long totalFallidos = recordatoriosPropiedad.stream()
                .filter(r -> r.getEstado() == Recordatorio.EstadoRecordatorio.FALLIDO)
                .count();
        
        long totalCancelados = recordatoriosPropiedad.stream()
                .filter(r -> r.getEstado() == Recordatorio.EstadoRecordatorio.CANCELADO)
                .count();
        
        // Calcular tasa de éxito
        long totalEnviosIntentados = totalEnviados + totalFallidos;
        double tasaExito = totalEnviosIntentados > 0 
                ? (totalEnviados * 100.0) / totalEnviosIntentados 
                : 0.0;
        
        // Contar recordatorios programados para hoy
        LocalDate hoy = LocalDate.now();
        long proximosEnvios = recordatoriosPropiedad.stream()
                .filter(r -> r.getEstado() == Recordatorio.EstadoRecordatorio.PROGRAMADO)
                .filter(r -> r.getProgramadoPara().toLocalDate().equals(hoy))
                .count();
        
        return EstadisticasRecordatoriosDto.builder()
                .totalProgramados(totalProgramados)
                .totalEnviados(totalEnviados)
                .totalFallidos(totalFallidos)
                .totalCancelados(totalCancelados)
                .tasaExito(tasaExito)
                .proximosEnvios(proximosEnvios)
                .build();
    }

    /**
     * Cancela un recordatorio programado.
     */
    public void cancelarRecordatorio(Long recordatorioId) {
        Recordatorio recordatorio = recordatorioRepository.findById(recordatorioId)
                .orElseThrow(() -> new RuntimeException("Recordatorio no encontrado"));
        
        if (recordatorio.getEstado() != Recordatorio.EstadoRecordatorio.PROGRAMADO) {
            throw new RuntimeException("Solo se pueden cancelar recordatorios en estado PROGRAMADO");
        }
        
        recordatorio.setEstado(Recordatorio.EstadoRecordatorio.CANCELADO);
        recordatorioRepository.save(recordatorio);
        
        log.info("Recordatorio {} cancelado", recordatorioId);
    }

    /**
     * Convierte un Recordatorio a RecordatorioDto.
     */
    private RecordatorioDto convertirADto(Recordatorio recordatorio) {
        return RecordatorioDto.builder()
                .id(recordatorio.getId())
                .facturaId(recordatorio.getFactura().getId())
                .contratoId(recordatorio.getContrato().getId())
                .inquilinoId(recordatorio.getContrato().getInquilino().getId())
                .inquilinoNombre(recordatorio.getContrato().getInquilino().getNombreCompleto())
                .habitacionCodigo(recordatorio.getContrato().getHabitacion().getCodigo())
                .programadoPara(recordatorio.getProgramadoPara())
                .enviadoEn(recordatorio.getEnviadoEn())
                .canal(recordatorio.getCanal().name())
                .telefonoDestino(recordatorio.getTelefonoDestino())
                .mensaje(recordatorio.getMensaje())
                .idMensajeProveedor(recordatorio.getIdMensajeProveedor())
                .estado(recordatorio.getEstado().name())
                                .tipo(recordatorio.getTipo().name())
                                .creadoPorUsuarioId(recordatorio.getCreadoPorUsuarioId())
                .build();
    }
}
