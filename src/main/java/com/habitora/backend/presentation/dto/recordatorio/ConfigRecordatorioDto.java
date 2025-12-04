package com.habitora.backend.presentation.dto.recordatorio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigRecordatorioDto {
    private Long propiedadId;
    private Integer diasAntes; // días de anticipación para creación automática
    private String canal; // WHATSAPP
    private String telefonoRemitente; // opcional
    private Boolean estaActivo; // ON/OFF
    private String horaEnvio; // HH:mm
    private String offsets; // CSV de offsets, ejemplo "-3,-2,-1,0,1,2"
    private String mensajeTemplate; // plantilla por defecto
}
