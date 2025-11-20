package com.habitora.backend.service.interfaces;

import com.habitora.backend.presentation.dto.habitacion.request.HabitacionCreateRequestDto;
import com.habitora.backend.presentation.dto.habitacion.response.HabitacionResponseDto;
import java.util.List;

public interface IHabitacionService {

    List<HabitacionResponseDto> createAutomatic(HabitacionCreateRequestDto dto);

    List<HabitacionResponseDto> getByPiso(Long pisoId);
}
