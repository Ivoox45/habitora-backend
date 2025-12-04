package com.habitora.backend.presentation.dto.recordatorio;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordatorioDto {
    
    private Long id;
    private Long facturaId;
    private Long contratoId;
    private Long inquilinoId;
    private String inquilinoNombre;
    private String habitacionCodigo;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime programadoPara;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime enviadoEn;
    
    private String canal;
    private String telefonoDestino;
    private String mensaje;
    private String idMensajeProveedor;
    private String estado;
    private String tipo;
    private Long creadoPorUsuarioId;
}
