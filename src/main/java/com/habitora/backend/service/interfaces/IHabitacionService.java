package com.habitora.backend.service.interfaces;

import com.habitora.backend.persistence.entity.Habitacion;
import com.habitora.backend.presentation.dto.habitacion.request.HabitacionCreateRequestDto;
import com.habitora.backend.presentation.dto.habitacion.request.HabitacionManualCreateRequestDto;
import com.habitora.backend.presentation.dto.habitacion.request.HabitacionUpdateRequestDto;
import com.habitora.backend.presentation.dto.habitacion.response.HabitacionResponseDto;
import com.habitora.backend.presentation.dto.habitacion.response.PisoHabitacionesResponseDto;

import java.util.List;

public interface IHabitacionService {

    List<HabitacionResponseDto> createAutomatic(HabitacionCreateRequestDto dto);

    List<HabitacionResponseDto> getByPiso(Long pisoId);

    List<PisoHabitacionesResponseDto> getByPropiedadGroupedByPiso(
            Long propiedadId,
            Habitacion.EstadoHabitacion estadoFilter,
            String searchCodigo);

    HabitacionResponseDto createManual(
            Long propiedadId,
            HabitacionManualCreateRequestDto request);

    HabitacionResponseDto update(
            Long propiedadId,
            Long habitacionId,
            HabitacionUpdateRequestDto request);

    void delete(
            Long propiedadId,
            Long habitacionId);

}
