package com.habitora.backend.service.interfaces;

import com.habitora.backend.presentation.dto.inquilino.request.InquilinoCreateRequestDto;
import com.habitora.backend.presentation.dto.inquilino.request.InquilinoUpdateRequestDto;
import com.habitora.backend.presentation.dto.inquilino.response.InquilinoListResponseDto;
import com.habitora.backend.presentation.dto.inquilino.response.InquilinoResponseDto;

import java.util.List;
import java.util.Optional;

public interface IInquilinoService {

    InquilinoResponseDto create(Long propiedadId, InquilinoCreateRequestDto request);

    List<InquilinoListResponseDto> findAll(Long propiedadId);

    Optional<InquilinoResponseDto> findById(Long propiedadId, Long id);

    InquilinoResponseDto update(Long propiedadId, Long id, InquilinoUpdateRequestDto request);

    void deleteById(Long propiedadId, Long id);

    List<InquilinoListResponseDto> search(Long propiedadId, String query);
}
