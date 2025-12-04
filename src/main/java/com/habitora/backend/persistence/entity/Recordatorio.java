package com.habitora.backend.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

@Entity
@Table(name = "recordatorios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@ToString
@EqualsAndHashCode
public class Recordatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @NotNull(message = "El recordatorio debe estar vinculado a una factura.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factura_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_recordatorio_factura"))
    private Factura factura;

    @NotNull(message = "El recordatorio debe estar vinculado a un contrato.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_recordatorio_contrato"))
    private Contrato contrato;

    @NotNull(message = "Debe especificarse la fecha y hora de programación del recordatorio.")
    @Column(name = "programado_para", nullable = false)
    private LocalDateTime programadoPara;

    @Column(name = "enviado_en")
    private LocalDateTime enviadoEn;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal", nullable = false, length = 20)
    private Canal canal;

    @NotNull(message = "El número de teléfono destino es obligatorio.")
    @Column(name = "telefono_destino", nullable = false, length = 40)
    private String telefonoDestino;

    @Lob
    @Column(name = "mensaje", nullable = true)
    private String mensaje;

    @Column(name = "id_mensaje_proveedor", length = 120)
    private String idMensajeProveedor;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoRecordatorio estado;

    // Indica si fue generado automáticamente por el sistema o manualmente por el usuario
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoRecordatorio tipo = TipoRecordatorio.AUTOMATICO;

    // Usuario que creó el recordatorio manual (nullable para automáticos)
    @Column(name = "creado_por_usuario_id")
    private Long creadoPorUsuarioId;

    public enum TipoRecordatorio {
        AUTOMATICO,
        MANUAL
    }
    
    public enum Canal {
        WHATSAPP
    }

    public enum EstadoRecordatorio {
        PROGRAMADO,
        ENVIADO,
        FALLIDO,
        CANCELADO
    }
}
