package com.habitora.backend.service.interfaces;

import com.habitora.backend.presentation.dto.propiedad.request.PropiedadCreateRequestDto;
import com.habitora.backend.presentation.dto.propiedad.request.PropiedadUpdateRequestDto;
import com.habitora.backend.presentation.dto.propiedad.response.PropiedadListResponseDto;
import com.habitora.backend.presentation.dto.propiedad.response.PropiedadResponseDto;

import java.util.List;
import java.util.Optional;

public interface IPropiedadService {

    PropiedadResponseDto createForCurrentUser(PropiedadCreateRequestDto dto);

    List<PropiedadListResponseDto> findAllForCurrentUser();

    Optional<PropiedadResponseDto> findByIdForCurrentUser(Long id);

    PropiedadResponseDto updateForCurrentUser(Long id, PropiedadUpdateRequestDto dto);

    void deleteForCurrentUser(Long id);
}
