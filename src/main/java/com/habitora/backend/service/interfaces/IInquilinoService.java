package com.habitora.backend.service.interfaces;

import com.habitora.backend.presentation.dto.inquilino.request.InquilinoCreateRequestDto;
import com.habitora.backend.presentation.dto.inquilino.request.InquilinoUpdateRequestDto;
import com.habitora.backend.presentation.dto.inquilino.response.InquilinoListResponseDto;
import com.habitora.backend.presentation.dto.inquilino.response.InquilinoResponseDto;

import java.util.List;
import java.util.Optional;

public interface IInquilinoService {

    InquilinoResponseDto create(InquilinoCreateRequestDto request);

    List<InquilinoListResponseDto> findAll();

    Optional<InquilinoResponseDto> findById(Long id);

    InquilinoResponseDto update(Long id, InquilinoUpdateRequestDto request);

    void deleteById(Long id);

    List<InquilinoListResponseDto> search(String query);

}
