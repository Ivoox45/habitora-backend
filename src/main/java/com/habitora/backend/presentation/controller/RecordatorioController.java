package com.habitora.backend.presentation.controller;

import com.habitora.backend.presentation.dto.recordatorio.EstadisticasRecordatoriosDto;
import com.habitora.backend.presentation.dto.recordatorio.RecordatorioDto;
import com.habitora.backend.presentation.dto.recordatorio.ConfigRecordatorioDto;
import com.habitora.backend.persistence.entity.ConfigRecordatorio;
import com.habitora.backend.persistence.repository.ConfigRecordatorioRepository;
import com.habitora.backend.presentation.dto.recordatorio.CrearRecordatoriosManualRequest;
import com.habitora.backend.service.implementation.RecordatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/propiedades/{propiedadId}/recordatorios")
@Tag(name = "Recordatorios", description = "Gestión de recordatorios automáticos de pago por WhatsApp")
@RequiredArgsConstructor
public class RecordatorioController {

    private final RecordatorioService recordatorioService;
        private final ConfigRecordatorioRepository configRecordatorioRepository;

    @GetMapping
    @Operation(summary = "Listar recordatorios", description = """
            Lista los recordatorios de la propiedad.
            Filtros opcionales:
            - estado: PROGRAMADO, ENVIADO, FALLIDO, CANCELADO
            - tipo: AUTOMATICO, MANUAL
            - fechaDesde: fecha inicio (yyyy-MM-dd)
            - fechaHasta: fecha fin (yyyy-MM-dd)
            - inquilinoNombre: buscar por nombre de inquilino
            """)
    @ApiResponse(responseCode = "200", description = "Lista de recordatorios", 
                 content = @Content(mediaType = "application/json", 
                 array = @ArraySchema(schema = @Schema(implementation = RecordatorioDto.class))))
    public ResponseEntity<List<RecordatorioDto>> listar(
            @PathVariable Long propiedadId,
            @RequestParam(required = false) String estado,
                        @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            @RequestParam(required = false) String inquilinoNombre) {
        
                List<RecordatorioDto> recordatorios = recordatorioService.listarRecordatorios(
                                propiedadId, estado, tipo, fechaDesde, fechaHasta, inquilinoNombre);
        
        return ResponseEntity.ok(recordatorios);
    }

        @GetMapping("/config")
        @Operation(summary = "Obtener configuración de recordatorios")
        public ResponseEntity<ConfigRecordatorioDto> obtenerConfig(@PathVariable Long propiedadId) {
                ConfigRecordatorio config = configRecordatorioRepository.findByPropiedadId(propiedadId)
                                .orElse(null);
                ConfigRecordatorioDto dto = config == null
                                ? ConfigRecordatorioDto.builder()
                                        .propiedadId(propiedadId)
                                        .diasAntes(3)
                                        .canal("WHATSAPP")
                                        .estaActivo(true)
                                        .horaEnvio("08:00")
                                        .offsets("-3,-2,-1,0,1,2")
                                        .build()
                                : ConfigRecordatorioDto.builder()
                                        .propiedadId(propiedadId)
                                        .diasAntes(config.getDiasAntes())
                                        .canal(config.getCanal().name())
                                        .telefonoRemitente(config.getTelefonoRemitente())
                                        .estaActivo(config.getEstaActivo())
                                        .horaEnvio(config.getHoraEnvio() != null ? config.getHoraEnvio().toString() : null)
                                        .offsets(config.getOffsetsCsv())
                                        .mensajeTemplate(config.getMensajeTemplate())
                                        .build();
                return ResponseEntity.ok(dto);
        }

        @PutMapping("/config")
        @Operation(summary = "Actualizar configuración de recordatorios")
        public ResponseEntity<Void> actualizarConfig(
                        @PathVariable Long propiedadId,
                        @RequestBody ConfigRecordatorioDto body) {
                ConfigRecordatorio config = configRecordatorioRepository.findByPropiedadId(propiedadId)
                                .orElseGet(() -> ConfigRecordatorio.builder()
                                                .propiedad(com.habitora.backend.persistence.entity.Propiedad.builder().id(propiedadId).build())
                                                .canal(ConfigRecordatorio.Canal.WHATSAPP)
                                                .diasAntes(3)
                                                .estaActivo(true)
                                                .build());
                if (body.getDiasAntes() != null) config.setDiasAntes(body.getDiasAntes());
                if (body.getTelefonoRemitente() != null) config.setTelefonoRemitente(body.getTelefonoRemitente());
                if (body.getEstaActivo() != null) config.setEstaActivo(body.getEstaActivo());
                if (body.getCanal() != null) config.setCanal(ConfigRecordatorio.Canal.valueOf(body.getCanal()));
                if (body.getHoraEnvio() != null) config.setHoraEnvio(java.time.LocalTime.parse(body.getHoraEnvio()));
                if (body.getOffsets() != null) config.setOffsetsCsv(body.getOffsets());
                if (body.getMensajeTemplate() != null) config.setMensajeTemplate(body.getMensajeTemplate());
                configRecordatorioRepository.save(config);
                return ResponseEntity.ok().build();
        }

        @PutMapping("/config/toggle")
        @Operation(summary = "Activar/Desactivar recordatorios")
        public ResponseEntity<Void> toggleConfig(
                        @PathVariable Long propiedadId,
                        @RequestParam boolean enabled) {
                ConfigRecordatorio config = configRecordatorioRepository.findByPropiedadId(propiedadId)
                                .orElseGet(() -> ConfigRecordatorio.builder()
                                                .propiedad(com.habitora.backend.persistence.entity.Propiedad.builder().id(propiedadId).build())
                                                .canal(ConfigRecordatorio.Canal.WHATSAPP)
                                                .diasAntes(3)
                                                .estaActivo(true)
                                                .build());
                config.setEstaActivo(enabled);
                configRecordatorioRepository.save(config);
                return ResponseEntity.ok().build();
        }

        @GetMapping("/{recordatorioId}")
        @Operation(summary = "Obtener recordatorio por ID", description = "Obtiene un recordatorio específico por ID dentro de la propiedad indicada")
        @ApiResponse(responseCode = "200", description = "Recordatorio encontrado",
                                 content = @Content(mediaType = "application/json",
                                 schema = @Schema(implementation = RecordatorioDto.class)))
        @ApiResponse(responseCode = "404", description = "Recordatorio no encontrado")
        public ResponseEntity<RecordatorioDto> obtenerPorId(
                        @PathVariable Long propiedadId,
                        @PathVariable Long recordatorioId) {

                RecordatorioDto dto = recordatorioService.obtenerRecordatorioPorId(propiedadId, recordatorioId);
                return ResponseEntity.ok(dto);
        }

    @GetMapping("/estadisticas")
    @Operation(summary = "Obtener estadísticas", description = """
            Obtiene estadísticas de recordatorios de la propiedad:
            - Total programados, enviados, fallidos, cancelados
            - Tasa de éxito (porcentaje)
            - Próximos envíos programados para hoy
            """)
    @ApiResponse(responseCode = "200", description = "Estadísticas de recordatorios",
                 content = @Content(mediaType = "application/json",
                 schema = @Schema(implementation = EstadisticasRecordatoriosDto.class)))
    public ResponseEntity<EstadisticasRecordatoriosDto> obtenerEstadisticas(
            @PathVariable Long propiedadId) {
        
        EstadisticasRecordatoriosDto estadisticas = recordatorioService.obtenerEstadisticas(propiedadId);
        return ResponseEntity.ok(estadisticas);
    }

    @PutMapping("/{recordatorioId}/cancelar")
    @Operation(summary = "Cancelar recordatorio", description = """
            Cancela un recordatorio programado.
            Solo se pueden cancelar recordatorios en estado PROGRAMADO.
            """)
    @ApiResponse(responseCode = "200", description = "Recordatorio cancelado exitosamente")
    @ApiResponse(responseCode = "400", description = "No se puede cancelar el recordatorio")
    @ApiResponse(responseCode = "404", description = "Recordatorio no encontrado")
    public ResponseEntity<Void> cancelar(
            @PathVariable Long propiedadId,
            @PathVariable Long recordatorioId) {
        
        recordatorioService.cancelarRecordatorio(recordatorioId);
        return ResponseEntity.ok().build();
    }

        @PostMapping("/test-send")
        @Operation(summary = "Enviar mensaje de prueba", description = "Envía un mensaje de WhatsApp de prueba a un número específico para validar la configuración")
        public ResponseEntity<Void> testSend(
                        @PathVariable Long propiedadId,
                        @RequestBody java.util.Map<String, String> body) {
                String telefono = body.get("telefonoDestino");
                
                if (telefono == null || telefono.isBlank()) {
                        return ResponseEntity.badRequest().build();
                }
                // Nota: por ahora solo validamos la entrada y respondemos 200.
                // En la siguiente iteración inyectaremos WhatsAppService para enviar la prueba realmente.
                return ResponseEntity.ok().build();
        }

        @PostMapping("/procesar-hoy")
        @Operation(summary = "Procesar recordatorios de hoy", description = """
                        Crea recordatorios para las facturas abiertas cuyo vencimiento
                        esté dentro de los rangos (-3, -2, -1, 0, +1, +2) relativos a hoy.
                        Útil para pruebas manuales desde el panel.
                        """)
        @ApiResponse(responseCode = "200", description = "Cantidad de recordatorios creados")
        public ResponseEntity<Integer> procesarHoy(@PathVariable Long propiedadId) {
                int creados = recordatorioService.procesarRecordatoriosDeHoy();
                return ResponseEntity.ok(creados);
        }

        @PostMapping("/manual")
        @Operation(summary = "Crear recordatorios manuales", description = """
                        Crea recordatorios manuales para uno o varios inquilinos activos
                        de la propiedad aplicando un patrón de días relativo al vencimiento.
                        Patrones: COMPLETO, SOLO_ANTES, SOLO_VENCIMIENTO, SOLO_DESPUES, PERSONALIZADO.
                        Placeholders soportados en mensaje: {nombre} {monto} {habitacion} {dias}.
                        """)
        @ApiResponse(responseCode = "200", description = "Cantidad de recordatorios creados")
        public ResponseEntity<Integer> crearManual(
                        @PathVariable Long propiedadId,
                        @RequestBody CrearRecordatoriosManualRequest request,
                        @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioId) {
                int creados = recordatorioService.crearRecordatoriosManuales(
                                propiedadId,
                                request.getInquilinoIds(),
                                request.getPatron(),
                                request.getMensajePersonalizado(),
                                request.getHoraEnvio(),
                                request.getFechaPersonalizada() != null ? java.time.LocalDate.parse(request.getFechaPersonalizada()) : null,
                                usuarioId
                );
                return ResponseEntity.ok(creados);
        }
}
