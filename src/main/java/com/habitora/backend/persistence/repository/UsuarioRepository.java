package com.habitora.backend.persistence.repository;

import com.habitora.backend.persistence.entity.Usuario;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmail(String email);

    Optional<Usuario> findByEmail(String email);

    @Query("SELECT COUNT(p) FROM Propiedad p WHERE p.usuario.id = :usuarioId")
    long countPropiedadesByUsuarioId(@Param("usuarioId") Long usuarioId);
    

}
