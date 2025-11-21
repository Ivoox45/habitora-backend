package com.habitora.backend.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pagos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@ToString(exclude = {"contrato", "factura"})
@EqualsAndHashCode(exclude = {"contrato", "factura"})
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    // ============================
    // RELACIONES
    // ============================

    @NotNull(message = "El pago debe estar asociado a un contrato.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "contrato_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_pago_contrato")
    )
    private Contrato contrato;

    /**
     * Para pagos de renta mensual se asocia a una factura.
     * Para pagos de depósito u otros, puede ser null.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "factura_id",
            foreignKey = @ForeignKey(name = "fk_pago_factura")
    )
    private Factura factura;

    // ============================
    // DATOS DEL PAGO
    // ============================

    @NotNull(message = "La fecha del pago es obligatoria.")
    @Column(name = "fecha_pago", nullable = false)
    private LocalDate fechaPago;

    @NotNull(message = "El monto del pago es obligatorio.")
    @Positive(message = "El monto debe ser mayor a cero.")
    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo", nullable = false, length = 20)
    private MetodoPago metodo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPago estado;

    // ============================
    // FIRMA DIGITAL DEL PAGO
    // ============================

    /**
     * Firma del inquilino para confirmar este pago
     * (diferente de la firma del contrato).
     */
    @Lob
    @Column(name = "firma_inquilino", columnDefinition = "LONGBLOB")
    private byte[] firmaInquilino;

    // ============================
    // ENUMS
    // ============================

    public enum MetodoPago {
        EFECTIVO,
        TRANSFERENCIA,
        YAPE,
        PLIN,
        OTRO
    }

    public enum EstadoPago {
        COMPLETADO,
        ANULADO
    }
}
