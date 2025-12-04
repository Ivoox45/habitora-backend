package com.habitora.backend.presentation.dto.recordatorio;

import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class CrearRecordatoriosManualRequest {
    private List<Long> inquilinoIds; // Inquilinos seleccionados
    private String patron; // COMPLETO | SOLO_ANTES | SOLO_VENCIMIENTO | SOLO_DESPUES | PERSONALIZADO
    private String mensajePersonalizado; // Puede contener placeholders {nombre} {monto} {habitacion} {dias}
    private LocalTime horaEnvio; // Hora deseada para cada recordatorio del patrón (excepto PERSONALIZADO puede usar programadoPara exacto)
    private String fechaPersonalizada; // yyyy-MM-dd usado solo si patron = PERSONALIZADO
}
