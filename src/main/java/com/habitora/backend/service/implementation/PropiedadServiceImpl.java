package com.habitora.backend.service.implementation;

import com.habitora.backend.persistence.entity.Piso;
import com.habitora.backend.persistence.entity.Propiedad;
import com.habitora.backend.persistence.entity.Usuario;

import com.habitora.backend.persistence.repository.PropiedadRepository;
import com.habitora.backend.persistence.repository.PisoRepository;

import com.habitora.backend.presentation.dto.propiedad.request.PropiedadCreateRequestDto;
import com.habitora.backend.presentation.dto.propiedad.request.PropiedadUpdateRequestDto;
import com.habitora.backend.presentation.dto.propiedad.response.PropiedadListResponseDto;
import com.habitora.backend.presentation.dto.propiedad.response.PropiedadResponseDto;

import com.habitora.backend.service.interfaces.IPropiedadService;
import com.habitora.backend.util.mapper.PropiedadMapper;

import com.habitora.backend.util.security.SecurityHelper;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropiedadServiceImpl implements IPropiedadService {

    private final PropiedadRepository propiedadRepository;
    private final PisoRepository pisoRepository;
    private final PropiedadMapper propiedadMapper;
    private final SecurityHelper securityHelper;

    // ==========================
    // Helpers
    // ==========================

    // ==========================
    // Helpers
    // ==========================

    private void validateBusinessRules(Propiedad propiedad) {
        Integer cantidadPisos = propiedad.getCantidadPisos();
        Integer pisoResidencia = propiedad.getPisoResidenciaDueno();

        if (cantidadPisos != null && pisoResidencia != null) {
            if (pisoResidencia > cantidadPisos) {
                throw new IllegalArgumentException("El piso de residencia no puede ser mayor a la cantidad de pisos.");
            }
        }
    }

    // ==========================
    // Implementaciones
    // ==========================

    @Override
    @Transactional
    public PropiedadResponseDto createForCurrentUser(PropiedadCreateRequestDto dto) {
        Usuario currentUser = securityHelper.getCurrentUser();

        // Convertir DTO → Entidad
        Propiedad propiedad = propiedadMapper.toEntity(dto);
        propiedad.setUsuario(currentUser);

        // Validaciones de negocio
        validateBusinessRules(propiedad);

        // Guardar propiedad primero (sin pisos aún)
        Propiedad guardada = propiedadRepository.save(propiedad);

        // Crear automáticamente los pisos
        int cantidadPisos = guardada.getCantidadPisos();

        for (int num = 1; num <= cantidadPisos; num++) {

            Piso piso = Piso.builder()
                    .propiedad(guardada)
                    .numeroPiso(num)
                    .build();

            // Relación en memoria
            guardada.addPiso(piso);

            // Persistir piso
            pisoRepository.save(piso);
        }

        return propiedadMapper.toResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropiedadListResponseDto> findAllForCurrentUser() {
        Usuario currentUser = securityHelper.getCurrentUser();

        return propiedadRepository.findByUsuarioId(currentUser.getId())
                .stream()
                .map(propiedadMapper::toListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PropiedadResponseDto> findByIdForCurrentUser(Long id) {
        Usuario currentUser = securityHelper.getCurrentUser();

        return propiedadRepository.findByIdAndUsuarioId(id, currentUser.getId())
                .map(propiedadMapper::toResponse);
    }

    @Override
    @Transactional
    public PropiedadResponseDto updateForCurrentUser(Long id, PropiedadUpdateRequestDto dto) {
        Usuario currentUser = securityHelper.getCurrentUser();

        Propiedad propiedad = propiedadRepository.findByIdAndUsuarioId(id, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Propiedad no encontrada o no pertenece al usuario."));

        // Actualizar valores simples
        if (dto.getNombre() != null)
            propiedad.setNombre(dto.getNombre());
        if (dto.getDireccion() != null)
            propiedad.setDireccion(dto.getDireccion());

        if (dto.getCantidadPisos() != null) {
            propiedad.setCantidadPisos(dto.getCantidadPisos());
        }

        if (dto.getPisoResidenciaDueno() != null) {
            propiedad.setPisoResidenciaDueno(dto.getPisoResidenciaDueno());
        }

        validateBusinessRules(propiedad);

        Propiedad saved = propiedadRepository.save(propiedad);

        return propiedadMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteForCurrentUser(Long id) {
        Usuario currentUser = securityHelper.getCurrentUser();

        Propiedad propiedad = propiedadRepository.findByIdAndUsuarioId(id, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Propiedad no encontrada o no pertenece al usuario."));

        propiedadRepository.delete(propiedad);
    }
}
