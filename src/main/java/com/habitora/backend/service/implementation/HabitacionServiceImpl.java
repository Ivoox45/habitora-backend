package com.habitora.backend.service.implementation;

import com.habitora.backend.persistence.entity.Habitacion;
import com.habitora.backend.persistence.entity.Piso;
import com.habitora.backend.persistence.entity.Propiedad;
import com.habitora.backend.persistence.entity.Usuario;
import com.habitora.backend.persistence.repository.HabitacionRepository;
import com.habitora.backend.persistence.repository.PisoRepository;
import com.habitora.backend.persistence.repository.PropiedadRepository;
import com.habitora.backend.presentation.dto.habitacion.request.HabitacionCreateRequestDto;
import com.habitora.backend.presentation.dto.habitacion.request.HabitacionManualCreateRequestDto;
import com.habitora.backend.presentation.dto.habitacion.request.HabitacionUpdateRequestDto;
import com.habitora.backend.presentation.dto.habitacion.response.HabitacionResponseDto;
import com.habitora.backend.presentation.dto.habitacion.response.PisoHabitacionesResponseDto;
import com.habitora.backend.service.interfaces.IHabitacionService;
import com.habitora.backend.util.mapper.HabitacionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HabitacionServiceImpl implements IHabitacionService {

    private final PisoRepository pisoRepository;
    private final PropiedadRepository propiedadRepository;
    private final HabitacionRepository habitacionRepository;
    private final HabitacionMapper habitacionMapper;

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

    private Propiedad validarPropiedadDelUsuario(Long propiedadId) {
        Usuario actual = getCurrentUser();

        Propiedad propiedad = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new IllegalArgumentException("Propiedad no encontrada."));

        if (!Objects.equals(propiedad.getUsuario().getId(), actual.getId())) {
            throw new IllegalArgumentException("No tienes acceso a esta propiedad.");
        }

        return propiedad;
    }

    /*
     * =======================================================
     * CREACIÓN AUTOMÁTICA
     * =======================================================
     */

    @Override
    @Transactional
    public List<HabitacionResponseDto> createAutomatic(HabitacionCreateRequestDto dto) {

        Usuario actual = getCurrentUser();

        Piso piso = pisoRepository.findById(dto.getPisoId())
                .orElseThrow(() -> new IllegalArgumentException("Piso no encontrado."));

        Propiedad propiedad = piso.getPropiedad();

        if (!Objects.equals(propiedad.getUsuario().getId(), actual.getId())) {
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

    /*
     * =======================================================
     * GET POR PISO (ya existente, sin propiedadId en la URL)
     * =======================================================
     */

    @Override
    @Transactional(readOnly = true)
    public List<HabitacionResponseDto> getByPiso(Long pisoId) {
        return habitacionRepository.findByPisoId(pisoId)
                .stream()
                .map(habitacionMapper::toResponse)
                .toList();
    }

    /*
     * =======================================================
     * AGRUPAR POR PISO + FILTROS (endpoint principal)
     * =======================================================
     */

    @Override
    @Transactional(readOnly = true)
    public List<PisoHabitacionesResponseDto> getByPropiedadGroupedByPiso(
            Long propiedadId,
            Habitacion.EstadoHabitacion estadoFilter,
            String searchCodigo,
            Boolean requierePrecio) {

        // Valida que la propiedad es del usuario
        validarPropiedadDelUsuario(propiedadId);

        // Carga todas las habitaciones de la propiedad
        List<Habitacion> habitaciones = habitacionRepository.findByPropiedadIdOrderByPisoNumeroPisoAsc(propiedadId);

        // 🔍 Filtro: estado
        if (estadoFilter != null) {
            habitaciones = habitaciones.stream()
                    .filter(h -> h.getEstado() == estadoFilter)
                    .collect(Collectors.toList());
        }

        // 🔍 Filtro: search por código (startsWith)
        if (searchCodigo != null && !searchCodigo.isBlank()) {
            String prefix = searchCodigo.trim().toLowerCase();
            habitaciones = habitaciones.stream()
                    .filter(h -> h.getCodigo().toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }
        // 🔍 Filtro: precio requerido
        if (requierePrecio != null) {
            if (requierePrecio) {
                // SOLO habitaciones con precio > 0
                habitaciones = habitaciones.stream()
                        .filter(h -> h.getPrecioRenta() != null &&
                                h.getPrecioRenta().compareTo(BigDecimal.ZERO) > 0)
                        .collect(Collectors.toList());
            } else {
                // SOLO habitaciones con precio == 0
                habitaciones = habitaciones.stream()
                        .filter(h -> h.getPrecioRenta() != null &&
                                h.getPrecioRenta().compareTo(BigDecimal.ZERO) == 0)
                        .collect(Collectors.toList());
            }
        }

        // Agrupar por piso
        Map<Piso, List<Habitacion>> grouped = habitaciones.stream()
                .collect(Collectors.groupingBy(Habitacion::getPiso));

        List<PisoHabitacionesResponseDto> result = new ArrayList<>();

        for (Map.Entry<Piso, List<Habitacion>> entry : grouped.entrySet()) {
            Piso piso = entry.getKey();

            PisoHabitacionesResponseDto dto = new PisoHabitacionesResponseDto();
            dto.setPisoId(piso.getId());
            dto.setNumeroPiso(piso.getNumeroPiso());
            dto.setHabitaciones(
                    entry.getValue().stream()
                            .map(habitacionMapper::toResponse)
                            .collect(Collectors.toList()));

            result.add(dto);
        }

        // Ordenar por número de piso
        result.sort(Comparator.comparing(PisoHabitacionesResponseDto::getNumeroPiso));

        return result;
    }

    /*
     * =======================================================
     * CREACIÓN MANUAL
     * =======================================================
     */

    @Override
    @Transactional
    public HabitacionResponseDto createManual(Long propiedadId, HabitacionManualCreateRequestDto request) {

        Propiedad propiedad = validarPropiedadDelUsuario(propiedadId);

        Piso piso = pisoRepository.findById(request.getPisoId())
                .orElseThrow(() -> new IllegalArgumentException("Piso no encontrado."));

        if (!Objects.equals(piso.getPropiedad().getId(), propiedadId)) {
            throw new IllegalArgumentException("Ese piso no pertenece a esta propiedad.");
        }

        Habitacion h = Habitacion.builder()
                .propiedad(propiedad)
                .piso(piso)
                .codigo(request.getCodigo())
                .precioRenta(request.getPrecioRenta())
                .estado(Habitacion.EstadoHabitacion.DISPONIBLE)
                .build();

        Habitacion saved = habitacionRepository.save(h);
        return habitacionMapper.toResponse(saved);
    }

    /*
     * =======================================================
     * UPDATE
     * =======================================================
     */

    @Override
    @Transactional
    public HabitacionResponseDto update(
            Long propiedadId,
            Long habitacionId,
            HabitacionUpdateRequestDto request) {

        Usuario actual = getCurrentUser();

        Habitacion h = habitacionRepository.findById(habitacionId)
                .orElseThrow(() -> new IllegalArgumentException("Habitación no encontrada."));

        if (!Objects.equals(h.getPropiedad().getId(), propiedadId)) {
            throw new IllegalArgumentException("La habitación no pertenece a esta propiedad.");
        }

        if (!Objects.equals(h.getPropiedad().getUsuario().getId(), actual.getId())) {
            throw new IllegalArgumentException("No tienes acceso.");
        }

        h.setCodigo(request.getCodigo());
        h.setPrecioRenta(request.getPrecioRenta());

        Habitacion updated = habitacionRepository.save(h);
        return habitacionMapper.toResponse(updated);
    }

    /*
     * =======================================================
     * DELETE
     * =======================================================
     */

    @Override
    @Transactional
    public void delete(Long propiedadId, Long habitacionId) {

        Usuario actual = getCurrentUser();

        Habitacion h = habitacionRepository.findById(habitacionId)
                .orElseThrow(() -> new IllegalArgumentException("Habitación no encontrada."));

        if (!Objects.equals(h.getPropiedad().getId(), propiedadId)) {
            throw new IllegalArgumentException("La habitación no pertenece a esta propiedad.");
        }

        if (!Objects.equals(h.getPropiedad().getUsuario().getId(), actual.getId())) {
            throw new IllegalArgumentException("No tienes acceso.");
        }

        habitacionRepository.delete(h);
    }
}
