package com.habitora.backend.persistence.repository;

import com.habitora.backend.persistence.entity.Inquilino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InquilinoRepository extends JpaRepository<Inquilino, Long> {

    boolean existsByNumeroDni(String numeroDni);

    @Query("""
            SELECT i FROM Inquilino i
            WHERE LOWER(i.nombreCompleto) LIKE LOWER(CONCAT(:query, '%'))
               OR i.numeroDni LIKE CONCAT(:query, '%')
            """)
    List<Inquilino> searchByNombreOrDni(String query);

}
