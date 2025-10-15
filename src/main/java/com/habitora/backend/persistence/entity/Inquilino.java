package com.habitora.backend.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.Accessors;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "inquilinos",
        indexes = {
                @Index(name = "idx_inquilino_dni", columnList = "numero_dni")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_inquilino_dni", columnNames = "numero_dni")
        }
)
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
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @NotBlank(message = "El nombre completo del inquilino es obligatorio.")
    @Size(max = 140, message = "El nombre completo no puede exceder los 140 caracteres.")
    @Column(name = "nombre_completo", nullable = false, length = 140)
    private String nombreCompleto;

    @NotBlank(message = "El número de DNI es obligatorio.")
    @Size(max = 20, message = "El DNI no puede exceder los 20 caracteres.")
    @Column(name = "numero_dni", nullable = false, unique = true, length = 20)
    private String numeroDni;

    @Email(message = "El formato del correo electrónico no es válido.")
    @Size(max = 160, message = "El correo electrónico no puede exceder los 160 caracteres.")
    @Column(name = "email", length = 160)
    private String email;

    @Size(max = 40, message = "El número de WhatsApp no puede exceder los 40 caracteres.")
    @Column(name = "telefono_whatsapp", length = 40)
    private String telefonoWhatsapp;

    @Builder.Default
    @OneToMany(mappedBy = "inquilino", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contrato> contratos = new ArrayList<>();

    public void addContrato(Contrato contrato) {
        contratos.add(contrato);
        contrato.setInquilino(this);
    }
}
