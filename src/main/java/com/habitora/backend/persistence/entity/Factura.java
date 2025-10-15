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
@Table(
        name = "facturas",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_factura_periodo", columnNames = {"contrato_id", "periodo_inicio", "periodo_fin"})
        },
        indexes = {
                @Index(name = "idx_factura_estado", columnList = "estado"),
                @Index(name = "idx_factura_vencimiento", columnList = "fecha_vencimiento")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@ToString(exclude = {"pagos", "recordatorios"})
@EqualsAndHashCode(exclude = {"pagos", "recordatorios"})
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @NotNull(message = "La factura debe estar asociada a un contrato.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_factura_contrato"))
    private Contrato contrato;

    @NotNull(message = "La fecha de inicio del periodo es obligatoria.")
    @Column(name = "periodo_inicio", nullable = false)
    private LocalDate periodoInicio;

    @NotNull(message = "La fecha de fin del periodo es obligatoria.")
    @Column(name = "periodo_fin", nullable = false)
    private LocalDate periodoFin;

    @NotNull(message = "La fecha de vencimiento es obligatoria.")
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @NotNull(message = "El monto de renta es obligatorio.")
    @PositiveOrZero(message = "El monto de renta no puede ser negativo.")
    @Column(name = "monto_renta", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoRenta;

    @NotNull(message = "El monto total a pagar es obligatorio.")
    @PositiveOrZero(message = "El total a pagar no puede ser negativo.")
    @Column(name = "total_a_pagar", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAPagar;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoFactura estado;

    @Builder.Default
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pago> pagos = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Recordatorio> recordatorios = new ArrayList<>();

    public void addPago(Pago pago) {
        pagos.add(pago);
        pago.setFactura(this);
    }

    public void addRecordatorio(Recordatorio recordatorio) {
        recordatorios.add(recordatorio);
        recordatorio.setFactura(this);
    }

    public enum EstadoFactura {
        ABIERTA,
        PAGADA,
        VENCIDA,
        CANCELADA
    }
}
