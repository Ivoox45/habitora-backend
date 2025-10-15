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
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "propiedad_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_contrato_propiedad"))
    private Propiedad propiedad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "habitacion_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_contrato_habitacion"))
    private Habitacion habitacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inquilino_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_contrato_inquilino"))
    private Inquilino inquilino;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoContrato estado;

    @NotNull(message = "La fecha de inicio es obligatoria.")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @NotNull(message = "El monto del depósito es obligatorio.")
    @PositiveOrZero(message = "El monto del depósito no puede ser negativo.")
    @Column(name = "monto_deposito", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoDeposito;

    @Column(name = "url_archivo_firmado", length = 300)
    private String urlArchivoFirmado;

    @Builder.Default
    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Factura> facturas = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pago> pagos = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Recordatorio> recordatorios = new ArrayList<>();

    public void addFactura(Factura factura) {
        facturas.add(factura);
        factura.setContrato(this);
    }

    public void addPago(Pago pago) {
        pagos.add(pago);
        pago.setContrato(this);
    }

    public void addRecordatorio(Recordatorio recordatorio) {
        recordatorios.add(recordatorio);
        recordatorio.setContrato(this);
    }

    public enum EstadoContrato {
        ACTIVO,
        CANCELADO
    }
}
