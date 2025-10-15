package com.habitora.backend.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.Accessors;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una propiedad (casa o edificio)
 * administrada por un usuario dentro del sistema Habitora.
 */
@Entity
@Table(name = "propiedades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@ToString(exclude = { "pisos", "habitaciones", "contratos", "configRecordatorios" })
@EqualsAndHashCode(exclude = { "pisos", "habitaciones", "contratos", "configRecordatorios" })
public class Propiedad {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", updatable = false, nullable = false)
  private Long id;

  // Relación ManyToOne con Usuario
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "usuario_id", nullable = false, foreignKey = @ForeignKey(name = "fk_propiedad_usuario"))
  private Usuario usuario;

  @NotBlank(message = "El nombre de la propiedad es obligatorio.")
  @Size(max = 120, message = "El nombre no puede exceder los 120 caracteres.")
  @Column(name = "nombre", nullable = false, length = 120)
  private String nombre;

  @Size(max = 200, message = "La dirección no puede exceder los 200 caracteres.")
  @Column(name = "direccion", length = 200)
  private String direccion;

  @PositiveOrZero(message = "La cantidad de pisos no puede ser negativa.")
  @Column(name = "cantidad_pisos", nullable = false)
  private Integer cantidadPisos;

  @PositiveOrZero(message = "El piso de residencia debe ser un número válido.")
  @Column(name = "piso_residencia_dueno")
  private Integer pisoResidenciaDueno;

  // Relaciones inversas
  @Builder.Default
  @OneToMany(mappedBy = "propiedad", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Piso> pisos = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "propiedad", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Habitacion> habitaciones = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "propiedad", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Contrato> contratos = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "propiedad", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ConfigRecordatorio> configRecordatorios = new ArrayList<>();

  // Métodos utilitarios
  public void addPiso(Piso piso) {
    pisos.add(piso);
    piso.setPropiedad(this);
  }

  public void addHabitacion(Habitacion habitacion) {
    habitaciones.add(habitacion);
    habitacion.setPropiedad(this);
  }

  public void addContrato(Contrato contrato) {
    contratos.add(contrato);
    contrato.setPropiedad(this);
  }

  public void addConfigRecordatorio(ConfigRecordatorio config) {
    configRecordatorios.add(config);
    config.setPropiedad(this);
  }
}
