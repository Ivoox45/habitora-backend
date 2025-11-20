package com.habitora.backend.service.implementation;

import com.habitora.backend.persistence.entity.*;
import com.habitora.backend.persistence.repository.*;
import com.habitora.backend.presentation.dto.habitacion.request.HabitacionCreateRequestDto;
import com.habitora.backend.presentation.dto.habitacion.response.HabitacionResponseDto;
import com.habitora.backend.service.interfaces.IHabitacionService;
import com.habitora.backend.util.mapper.HabitacionMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HabitacionServiceImpl implements IHabitacionService {

    private final PisoRepository pisoRepository;
    private final HabitacionRepository habitacionRepository;
    private final HabitacionMapper habitacionMapper;

    private Usuario getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof Usuario usuario)) {
            throw new IllegalStateException("No hay usuario autenticado.");
        }
        return usuario;
    }

    @Override
    @Transactional
    public List<HabitacionResponseDto> createAutomatic(HabitacionCreateRequestDto dto) {
        Usuario actual = getCurrentUser();

        Piso piso = pisoRepository.findById(dto.getPisoId())
                .orElseThrow(() -> new IllegalArgumentException("Piso no encontrado."));

        Propiedad propiedad = piso.getPropiedad();

        if (!propiedad.getUsuario().getId().equals(actual.getId())) {
            throw new IllegalArgumentException("No puedes modificar una propiedad que no es tuya.");
        }

        long existentes = habitacionRepository.countByPisoId(piso.getId());
        int nuevas = dto.getCantidadHabitaciones();

        if (existentes + nuevas > 8) {
            throw new IllegalArgumentException("Máximo 8 habitaciones por piso.");
        }

        List<HabitacionResponseDto> result = new ArrayList<>();

        for (int i = 1; i <= nuevas; i++) {

            int correlativo = (int) existentes + i;
            String codigo = (piso.getNumeroPiso() * 100) + correlativo + "";

            Habitacion h = Habitacion.builder()
                .propiedad(propiedad)
                .piso(piso)
                .codigo(codigo)
                .estado(Habitacion.EstadoHabitacion.DISPONIBLE)
                .precioRenta(BigDecimal.ZERO) // valor default
                .build();

            Habitacion saved = habitacionRepository.save(h);
            result.add(habitacionMapper.toResponse(saved));
        }

        return result;
    }

    @Override
    public List<HabitacionResponseDto> getByPiso(Long pisoId) {
        return habitacionRepository.findByPisoId(pisoId)
                .stream()
                .map(habitacionMapper::toResponse)
                .toList();
    }
}
