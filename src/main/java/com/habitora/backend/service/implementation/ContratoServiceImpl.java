package com.habitora.backend.service.implementation;

import com.habitora.backend.persistence.entity.*;
import com.habitora.backend.persistence.repository.*;
import com.habitora.backend.presentation.dto.contrato.request.ContratoCreateRequestDto;
import com.habitora.backend.presentation.dto.contrato.response.ContratoDetailResponseDto;
import com.habitora.backend.presentation.dto.contrato.response.ContratoListResponseDto;
import com.habitora.backend.service.interfaces.IContratoService;
import com.habitora.backend.util.mapper.ContratoMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class ContratoServiceImpl implements IContratoService {

    private final ContratoRepository contratoRepository;
    private final PropiedadRepository propiedadRepository;
    private final HabitacionRepository habitacionRepository;
    private final InquilinoRepository inquilinoRepository;
    private final ContratoMapper mapper;

    /* =======================================================
       Helpers de seguridad
       ======================================================= */

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

    /* =======================================================
       CREATE
       ======================================================= */
    @Override
    public ContratoDetailResponseDto create(Long propiedadId, ContratoCreateRequestDto request) {

        Propiedad propiedad = validarPropiedadDelUsuario(propiedadId);

        Inquilino inquilino = inquilinoRepository.findById(request.getInquilinoId())
                .orElseThrow(() -> new IllegalArgumentException("Inquilino no encontrado."));

        if (!Objects.equals(inquilino.getPropiedad().getId(), propiedadId)) {
            throw new IllegalArgumentException("Ese inquilino no pertenece a esta propiedad.");
        }

        Habitacion habitacion = habitacionRepository.findById(request.getHabitacionId())
                .orElseThrow(() -> new IllegalArgumentException("Habitación no encontrada."));

        if (!Objects.equals(habitacion.getPropiedad().getId(), propiedadId)) {
            throw new IllegalArgumentException("Esa habitación no pertenece a esta propiedad.");
        }

        // validar que no tenga contrato activo
        if (contratoRepository.existsActivoInHabitacion(habitacion.getId())) {
            throw new IllegalArgumentException("La habitación ya tiene un contrato activo.");
        }

        // crear contrato
        Contrato contrato = Contrato.builder()
                .propiedad(propiedad)
                .habitacion(habitacion)
                .inquilino(inquilino)
                .estado(Contrato.EstadoContrato.ACTIVO)
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .montoDeposito(request.getMontoDeposito())
                .firmaInquilino(null)
                .build();

        contratoRepository.save(contrato);

        // actualizar estado de la habitación
        habitacion.setEstado(Habitacion.EstadoHabitacion.OCUPADA);
        habitacionRepository.save(habitacion);

        return mapper.toDetailDto(contrato);
    }

    /* =======================================================
       LISTAR + FILTRAR
       ======================================================= */
    @Override
    @Transactional(readOnly = true)
    public List<ContratoListResponseDto> list(Long propiedadId, Contrato.EstadoContrato estado, String search) {

        validarPropiedadDelUsuario(propiedadId);

        List<Contrato> data = contratoRepository.searchContratos(propiedadId, estado, search);

        return data.stream()
                .map(mapper::toListDto)
                .toList();
    }

    /* =======================================================
       GET DETAIL
       ======================================================= */
    @Override
    @Transactional(readOnly = true)
    public ContratoDetailResponseDto getById(Long propiedadId, Long contratoId) {

        validarPropiedadDelUsuario(propiedadId);

        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new IllegalArgumentException("Contrato no encontrado."));

        if (!Objects.equals(contrato.getPropiedad().getId(), propiedadId)) {
            throw new IllegalArgumentException("Este contrato no pertenece a esta propiedad.");
        }

        return mapper.toDetailDto(contrato);
    }

    /* =======================================================
       FINALIZAR CONTRATO
       ======================================================= */
    @Override
    public ContratoDetailResponseDto finalizar(Long propiedadId, Long contratoId) {

        validarPropiedadDelUsuario(propiedadId);

        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new IllegalArgumentException("Contrato no encontrado."));

        if (!Objects.equals(contrato.getPropiedad().getId(), propiedadId)) {
            throw new IllegalArgumentException("Este contrato no pertenece a esta propiedad.");
        }

        contrato.setEstado(Contrato.EstadoContrato.CANCELADO);
        contratoRepository.save(contrato);

        // liberar habitación
        Habitacion hab = contrato.getHabitacion();
        hab.setEstado(Habitacion.EstadoHabitacion.DISPONIBLE);
        habitacionRepository.save(hab);

        return mapper.toDetailDto(contrato);
    }

    /* =======================================================
       SUBIR FIRMA DIGITAL
       ======================================================= */
    @Override
    public ContratoDetailResponseDto uploadFirma(Long propiedadId, Long contratoId, MultipartFile file)
            throws IOException {

        validarPropiedadDelUsuario(propiedadId);

        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new IllegalArgumentException("Contrato no encontrado."));

        if (!Objects.equals(contrato.getPropiedad().getId(), propiedadId)) {
            throw new IllegalArgumentException("Este contrato no pertenece a esta propiedad.");
        }

        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo de firma está vacío.");
        }

        contrato.setFirmaInquilino(file.getBytes());
        contratoRepository.save(contrato);

        return mapper.toDetailDto(contrato);
    }

    /* =======================================================
       OBTENER FIRMA
       ======================================================= */
    @Override
    @Transactional(readOnly = true)
    public byte[] getFirma(Long propiedadId, Long contratoId) {

        validarPropiedadDelUsuario(propiedadId);

        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new IllegalArgumentException("Contrato no encontrado."));

        if (!Objects.equals(contrato.getPropiedad().getId(), propiedadId)) {
            throw new IllegalArgumentException("Este contrato no pertenece a esta propiedad.");
        }

        if (contrato.getFirmaInquilino() == null) {
            throw new IllegalArgumentException("Este contrato no tiene firma.");
        }

        return contrato.getFirmaInquilino();
    }
}
