package com.habitora.backend.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.Accessors;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inquilinos", indexes = {
                @Index(name = "idx_inquilino_dni", columnList = "numero_dni")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@ToString(exclude = "contratos")
@EqualsAndHashCode(exclude = "contratos")
public class Inquilino {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NotNull(message = "El inquilino debe pertenecer a una propiedad.")
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "propiedad_id", nullable = false, foreignKey = @ForeignKey(name = "fk_inquilino_propiedad"))
        private Propiedad propiedad;

        @NotBlank
        @Size(max = 140)
        @Column(nullable = false, length = 140)
        private String nombreCompleto;

        @NotBlank
        @Size(max = 20)
        @Column(nullable = false, length = 20)
        private String numeroDni;

        @Email
        @Size(max = 160)
        private String email;

        @Size(max = 40)
        private String telefonoWhatsapp;

        @Builder.Default
        @OneToMany(mappedBy = "inquilino", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Contrato> contratos = new ArrayList<>();

        public void addContrato(Contrato contrato) {
                contratos.add(contrato);
                contrato.setInquilino(this);
        }
}
