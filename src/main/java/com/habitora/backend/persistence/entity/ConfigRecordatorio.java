package com.habitora.backend.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;

@Entity
@Table(name = "config_recordatorios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@ToString
@EqualsAndHashCode
public class ConfigRecordatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @NotNull(message = "La configuración debe pertenecer a una propiedad.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "propiedad_id", nullable = false, foreignKey = @ForeignKey(name = "fk_configrecordatorio_propiedad"))
    private Propiedad propiedad;

    @Min(value = 1, message = "El recordatorio debe enviarse al menos 1 día antes.")
    @Max(value = 30, message = "El recordatorio no puede configurarse con más de 30 días de anticipación.")
    @Column(name = "dias_antes", nullable = false)
    private Integer diasAntes;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal", nullable = false, length = 20)
    private Canal canal;

    @Column(name = "telefono_remitente", length = 40)
    private String telefonoRemitente;

    @Builder.Default
    @Column(name = "esta_activo", nullable = false)
    private Boolean estaActivo = true;

    // Hora preferida de envío de recordatorios automáticos
    @Column(name = "hora_envio")
    private java.time.LocalTime horaEnvio;

    // Offsets relativos al vencimiento (e.g., -3,-2,-1,0,1,2)
    @Column(name = "offsets", length = 60)
    private String offsetsCsv; // almacenado como CSV

    // Plantilla de mensaje por defecto con placeholders
    @Lob
    @Column(name = "mensaje_template")
    private String mensajeTemplate;

    public enum Canal {
        WHATSAPP
    }
}
