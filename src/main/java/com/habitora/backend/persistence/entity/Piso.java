package com.habitora.backend.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.Accessors;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pisos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@ToString(exclude = "habitaciones")
@EqualsAndHashCode(exclude = "habitaciones")
public class Piso {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", updatable = false, nullable = false)
  private Long id;

  @NotNull(message = "El piso debe pertenecer a una propiedad.")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "propiedad_id", nullable = false, foreignKey = @ForeignKey(name = "fk_piso_propiedad"))
  private Propiedad propiedad;

  @PositiveOrZero(message = "El número de piso debe ser 0 o positivo.")
  @Column(name = "numero_piso", nullable = false)
  private Integer numeroPiso;

  @Column(name = "codigo", length = 50)
  private String codigo;

  @Builder.Default
  @OneToMany(mappedBy = "piso", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Habitacion> habitaciones = new ArrayList<>();

  public void addHabitacion(Habitacion habitacion) {
    habitaciones.add(habitacion);
    habitacion.setPiso(this);
  }
}
