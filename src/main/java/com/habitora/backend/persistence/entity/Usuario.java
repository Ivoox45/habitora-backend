package com.habitora.backend.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;

@Entity
@Table(name = "usuarios", indexes = {
    @Index(name = "idx_usuario_email", columnList = "email")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_usuario_email", columnNames = { "email" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@ToString
@EqualsAndHashCode
public class Usuario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", updatable = false, nullable = false)
  private Long id;

  @NotBlank(message = "El nombre completo es obligatorio.")
  @Size(max = 120, message = "El nombre completo no puede exceder los 120 caracteres.")
  @Column(name = "nombre_completo", nullable = false, length = 120)
  private String nombreCompleto;

  @NotBlank(message = "El correo electrónico es obligatorio.")
  @Email(message = "El formato del correo electrónico no es válido.")
  @Column(name = "email", nullable = false, unique = true, length = 160)
  private String email;

  @Size(max = 40, message = "El número de WhatsApp no puede exceder los 40 caracteres.")
  @Column(name = "telefono_whatsapp", length = 40)
  private String telefonoWhatsapp;

  @NotBlank(message = "La contraseña es obligatoria.")
  @Size(min = 8, max = 255, message = "La contraseña debe tener entre 8 y 255 caracteres.")
  @Column(name = "contraseña", nullable = false, length = 255)
  private String contraseña;

}