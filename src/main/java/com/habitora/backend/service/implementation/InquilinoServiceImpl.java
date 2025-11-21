package com.habitora.backend.service.implementation;

import com.habitora.backend.persistence.entity.Inquilino;
import com.habitora.backend.persistence.repository.InquilinoRepository;
import com.habitora.backend.presentation.dto.inquilino.request.InquilinoCreateRequestDto;
import com.habitora.backend.presentation.dto.inquilino.request.InquilinoUpdateRequestDto;
import com.habitora.backend.presentation.dto.inquilino.response.InquilinoListResponseDto;
import com.habitora.backend.presentation.dto.inquilino.response.InquilinoResponseDto;
import com.habitora.backend.service.interfaces.IInquilinoService;
import com.habitora.backend.util.mapper.InquilinoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class InquilinoServiceImpl implements IInquilinoService {

    private final InquilinoRepository inquilinoRepository;
    private final InquilinoMapper mapper;

    @Override
    public InquilinoResponseDto create(InquilinoCreateRequestDto request) {
        if (inquilinoRepository.existsByNumeroDni(request.getNumeroDni())) {
            throw new IllegalArgumentException("Ya existe un inquilino con ese DNI.");
        }

        Inquilino entity = mapper.toEntity(request);
        inquilinoRepository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InquilinoListResponseDto> findAll() {
        return mapper.toListResponse(inquilinoRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InquilinoResponseDto> findById(Long id) {
        return inquilinoRepository.findById(id)
                .map(mapper::toResponse);
    }

    @Override
    public InquilinoResponseDto update(Long id, InquilinoUpdateRequestDto request) {
        Inquilino entity = inquilinoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inquilino no encontrado."));

        // Validación de DNI duplicado
        if (!entity.getNumeroDni().equals(request.getNumeroDni())
                && inquilinoRepository.existsByNumeroDni(request.getNumeroDni())) {
            throw new IllegalArgumentException("Ya existe un inquilino con ese DNI.");
        }

        mapper.updateEntity(entity, request);
        inquilinoRepository.save(entity);

        return mapper.toResponse(entity);
    }

    @Override
    public void deleteById(Long id) {
        Inquilino entity = inquilinoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inquilino no encontrado."));

        inquilinoRepository.delete(entity);
    }


    @Override
    @Transactional(readOnly = true)
    public List<InquilinoListResponseDto> search(String query) {

        if (query == null || query.trim().isEmpty()) {
            return mapper.toListResponse(inquilinoRepository.findAll());
        }

        List<Inquilino> result = inquilinoRepository.searchByNombreOrDni(query);
        return mapper.toListResponse(result);
    }

}
