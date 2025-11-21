package com.habitora.backend.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contratos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@ToString(exclude = {"facturas", "pagos", "recordatorios"})
@EqualsAndHashCode(exclude = {"facturas", "pagos", "recordatorios"})
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ============================
    // RELACIONES
    // ============================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "propiedad_id",
            foreignKey = @ForeignKey(name = "fk_contrato_propiedad"))
    private Propiedad propiedad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "habitacion_id",
            foreignKey = @ForeignKey(name = "fk_contrato_habitacion"))
    private Habitacion habitacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inquilino_id",
            foreignKey = @ForeignKey(name = "fk_contrato_inquilino"))
    private Inquilino inquilino;

    // ============================
    // ESTADO & FECHAS
    // ============================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoContrato estado;

    @NotNull
    @Column(nullable = false)
    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    // ============================
    // MONTO
    // ============================

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montoDeposito;

    // ============================
    // FIRMA DIGITAL (BLOB)
    // ============================

    @Lob
    @Column(name = "firma_inquilino", columnDefinition = "LONGBLOB")
    private byte[] firmaInquilino;

    // ============================
    // RELACIONES HIJAS
    // ============================

    @Builder.Default
    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Factura> facturas = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pago> pagos = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Recordatorio> recordatorios = new ArrayList<>();

    // ============================
    // ENUM
    // ============================

    public enum EstadoContrato {
        ACTIVO,
        CANCELADO
    }
}
