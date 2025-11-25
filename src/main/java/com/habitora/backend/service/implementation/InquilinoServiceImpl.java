package com.habitora.backend.service.implementation;

import com.habitora.backend.persistence.entity.Inquilino;
import com.habitora.backend.persistence.entity.Propiedad;
import com.habitora.backend.persistence.entity.Usuario;
import com.habitora.backend.persistence.repository.InquilinoRepository;
import com.habitora.backend.persistence.repository.PropiedadRepository;
import com.habitora.backend.presentation.dto.inquilino.request.InquilinoCreateRequestDto;
import com.habitora.backend.presentation.dto.inquilino.request.InquilinoUpdateRequestDto;
import com.habitora.backend.presentation.dto.inquilino.response.InquilinoListResponseDto;
import com.habitora.backend.presentation.dto.inquilino.response.InquilinoResponseDto;
import com.habitora.backend.service.interfaces.IInquilinoService;
import com.habitora.backend.util.mapper.InquilinoMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class InquilinoServiceImpl implements IInquilinoService {

    private final InquilinoRepository inquilinoRepository;
    private final PropiedadRepository propiedadRepository;
    private final InquilinoMapper mapper;

    /*
     * =======================================================
     * Helpers de seguridad
     * =======================================================
     */

    private Usuario getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Usuario usuario)) {
            throw new IllegalStateException("No hay usuario autenticado.");
        }
        return usuario;
    }

    private Propiedad validarPropiedad(Long propiedadId) {
        Usuario actual = getCurrentUser();

        Propiedad propiedad = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new IllegalArgumentException("Propiedad no encontrada."));

        if (!propiedad.getUsuario().getId().equals(actual.getId())) {
            throw new IllegalArgumentException("No tienes acceso a esta propiedad.");
        }

        return propiedad;
    }

    /*
     * =======================================================
     * CREATE
     * =======================================================
     */

    @Override
    public InquilinoResponseDto create(Long propiedadId, InquilinoCreateRequestDto request) {

        Propiedad propiedad = validarPropiedad(propiedadId);

        if (inquilinoRepository.existsByPropiedadAndDni(propiedadId, request.getNumeroDni())) {
            throw new IllegalArgumentException("Ya existe un inquilino con ese DNI en esta propiedad.");
        }

        Inquilino entity = mapper.toEntity(request);
        entity.setPropiedad(propiedad);

        inquilinoRepository.save(entity);

        return mapper.toResponse(entity);
    }

    /*
     * =======================================================
     * FIND ALL + FILTROS
     * =======================================================
     */

    @Override
    public List<InquilinoListResponseDto> findAll(Long propiedadId, Boolean disponibles, String query) {

        validarPropiedad(propiedadId);

        // FILTRO 1: disponibles = true (sin contrato activo)
        if (Boolean.TRUE.equals(disponibles)) {
            return mapper.toListResponse(inquilinoRepository.findDisponibles(propiedadId));
        }

        // FILTRO 2: búsqueda por texto
        if (query != null && !query.isBlank()) {
            return mapper.toListResponse(inquilinoRepository.search(propiedadId, query));
        }

        // DEFAULT
        return mapper.toListResponse(inquilinoRepository.findAllByPropiedad(propiedadId));
    }

    /*
     * =======================================================
     * FIND BY ID
     * =======================================================
     */

    @Override
    @Transactional(readOnly = true)
    public Optional<InquilinoResponseDto> findById(Long propiedadId, Long id) {

        validarPropiedad(propiedadId);

        return inquilinoRepository.findById(id)
                .filter(i -> i.getPropiedad().getId().equals(propiedadId))
                .map(mapper::toResponse);
    }

    /*
     * =======================================================
     * UPDATE
     * =======================================================
     */

    @Override
    public InquilinoResponseDto update(Long propiedadId, Long id, InquilinoUpdateRequestDto request) {

        validarPropiedad(propiedadId);

        Inquilino entity = inquilinoRepository.findById(id)
                .filter(i -> i.getPropiedad().getId().equals(propiedadId))
                .orElseThrow(() -> new IllegalArgumentException("Inquilino no encontrado."));

        // Validar DNI repetido dentro de la propiedad
        if (!entity.getNumeroDni().equals(request.getNumeroDni()) &&
                inquilinoRepository.existsByPropiedadAndDni(propiedadId, request.getNumeroDni())) {

            throw new IllegalArgumentException("Ya existe un inquilino con ese DNI en esta propiedad.");
        }

        mapper.updateEntity(entity, request);
        inquilinoRepository.save(entity);

        return mapper.toResponse(entity);
    }

    /*
     * =======================================================
     * DELETE
     * =======================================================
     */

    @Override
    public void deleteById(Long propiedadId, Long id) {

        validarPropiedad(propiedadId);

        Inquilino entity = inquilinoRepository.findById(id)
                .filter(i -> i.getPropiedad().getId().equals(propiedadId))
                .orElseThrow(() -> new IllegalArgumentException("Inquilino no encontrado."));

        inquilinoRepository.delete(entity);
    }

}
