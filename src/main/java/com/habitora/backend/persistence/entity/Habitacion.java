package com.habitora.backend.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.Accessors;
import java.math.BigDecimal;

@Entity
@Table(
        name = "habitaciones",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_codigo_propiedad", columnNames = {"propiedad_id", "codigo"})
        },
        indexes = {
                @Index(name = "idx_habitacion_codigo", columnList = "codigo")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@ToString(exclude = {"contratos"})
@EqualsAndHashCode(exclude = {"contratos"})
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @NotNull(message = "La habitación debe pertenecer a una propiedad.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "propiedad_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_habitacion_propiedad"))
    private Propiedad propiedad;

    @NotNull(message = "La habitación debe pertenecer a un piso.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "piso_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_habitacion_piso"))
    private Piso piso;

    @NotBlank(message = "El código de la habitación es obligatorio.")
    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @NotNull(message = "El precio de renta es obligatorio.")
    @PositiveOrZero(message = "El precio de renta no puede ser negativo.")
    @Column(name = "precio_renta", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioRenta;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoHabitacion estado;

    @Builder.Default
    @OneToMany(mappedBy = "habitacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Contrato> contratos = new java.util.ArrayList<>();

    public void addContrato(Contrato contrato) {
        contratos.add(contrato);
        contrato.setHabitacion(this);
    }

    public enum EstadoHabitacion {
        DISPONIBLE,
        OCUPADA
    }
}
