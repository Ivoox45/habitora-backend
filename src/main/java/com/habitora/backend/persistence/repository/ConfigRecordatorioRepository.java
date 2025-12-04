package com.habitora.backend.persistence.repository;

import com.habitora.backend.persistence.entity.ConfigRecordatorio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfigRecordatorioRepository extends JpaRepository<ConfigRecordatorio, Long> {
    Optional<ConfigRecordatorio> findByPropiedadId(Long propiedadId);
}
