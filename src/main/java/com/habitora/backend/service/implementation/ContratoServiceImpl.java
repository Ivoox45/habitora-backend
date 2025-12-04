package com.habitora.backend.service.implementation;

import com.habitora.backend.persistence.entity.*;
import com.habitora.backend.persistence.repository.*;
import com.habitora.backend.presentation.dto.contrato.request.ContratoCreateRequestDto;
import com.habitora.backend.presentation.dto.contrato.response.ContratoDetailResponseDto;
import com.habitora.backend.presentation.dto.contrato.response.ContratoListResponseDto;
import com.habitora.backend.service.interfaces.IFileStorageService;
import com.habitora.backend.service.interfaces.IContratoService;
import com.habitora.backend.service.interfaces.IFacturaService;
import com.habitora.backend.exception.BusinessException;
import com.habitora.backend.exception.ResourceNotFoundException;
import com.habitora.backend.util.mapper.ContratoMapper;
import com.habitora.backend.util.security.SecurityHelper;
import lombok.extern.slf4j.Slf4j;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ContratoServiceImpl implements IContratoService {

    private final ContratoRepository contratoRepository;
    private final PropiedadRepository propiedadRepository;
    private final HabitacionRepository habitacionRepository;
    private final InquilinoRepository inquilinoRepository;
    private final FacturaRepository facturaRepository;
    private final IFacturaService facturaService;
    private final IFileStorageService fileStorageService;
    private final ContratoMapper mapper;
    private final SecurityHelper securityHelper;

    /*
     * =======================================================
     * CREATE CONTRATO
     * =======================================================
     */
    @Override
    public ContratoDetailResponseDto create(Long propiedadId, ContratoCreateRequestDto request) {
        log.info("Creando contrato para propiedad ID: {} e inquilino ID: {}", propiedadId, request.getInquilinoId());

        Propiedad propiedad = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada."));
        securityHelper.validateOwner(propiedad);

        Inquilino inquilino = inquilinoRepository.findById(request.getInquilinoId())
                .orElseThrow(() -> new ResourceNotFoundException("Inquilino no encontrado."));

        if (!Objects.equals(inquilino.getPropiedad().getId(), propiedadId)) {
            throw new BusinessException("Ese inquilino no pertenece a esta propiedad.");
        }

        Habitacion habitacion = habitacionRepository.findById(request.getHabitacionId())
                .orElseThrow(() -> new ResourceNotFoundException("Habitación no encontrada."));

        if (!Objects.equals(habitacion.getPropiedad().getId(), propiedadId)) {
            throw new BusinessException("Esa habitación no pertenece a esta propiedad.");
        }

        // validar habitación sin contrato activo
        if (contratoRepository.existsActivoInHabitacion(habitacion.getId())) {
            throw new BusinessException("La habitación ya tiene un contrato activo.");
        }

        // Crear contrato
        Contrato contrato = Contrato.builder()
                .propiedad(propiedad)
                .habitacion(habitacion)
                .inquilino(inquilino)
                .estado(Contrato.EstadoContrato.ACTIVO)
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .montoDeposito(request.getMontoDeposito())
                .firmaPath(null)
                .build();

        contratoRepository.save(contrato);

        // Marcar habitación como ocupada
        habitacion.setEstado(Habitacion.EstadoHabitacion.OCUPADA);
        habitacionRepository.save(habitacion);

        // No generar facturas aún. Se generarán cuando el contrato sea firmado.

        log.info("Contrato creado exitosamente con ID: {}", contrato.getId());
        return mapper.toDetailDto(contrato);
    }

    /*
     * =======================================================
     * LISTAR CONTRATOS
     * =======================================================
     */
    @Override
    @Transactional(readOnly = true)
    public List<ContratoListResponseDto> list(Long propiedadId, Contrato.EstadoContrato estado, String search) {

        Propiedad propiedad = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada."));
        securityHelper.validateOwner(propiedad);

        List<Contrato> data = contratoRepository.searchContratos(propiedadId, estado, search);

        return data.stream()
                .map(mapper::toListDto)
                .toList();
    }

    /*
     * =======================================================
     * DETALLE DE CONTRATO
     * =======================================================
     */
    @Override
    @Transactional(readOnly = true)
    public ContratoDetailResponseDto getById(Long propiedadId, Long contratoId) {

        Propiedad propiedad = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada."));
        securityHelper.validateOwner(propiedad);

        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado."));

        if (!Objects.equals(contrato.getPropiedad().getId(), propiedadId)) {
            throw new BusinessException("Este contrato no pertenece a esta propiedad.");
        }

        return mapper.toDetailDto(contrato);
    }

    /*
     * =======================================================
     * FINALIZAR CONTRATO
     * =======================================================
     */
    @Override
    public ContratoDetailResponseDto finalizar(Long propiedadId, Long contratoId) {
        log.info("Finalizando contrato ID: {} de propiedad ID: {}", contratoId, propiedadId);

        Propiedad propiedad = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada."));
        securityHelper.validateOwner(propiedad);

        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado."));

        if (!Objects.equals(contrato.getPropiedad().getId(), propiedadId)) {
            throw new BusinessException("Este contrato no pertenece a esta propiedad.");
        }

        contrato.setEstado(Contrato.EstadoContrato.CANCELADO);
        contratoRepository.save(contrato);

        // liberar habitación
        Habitacion hab = contrato.getHabitacion();
        hab.setEstado(Habitacion.EstadoHabitacion.DISPONIBLE);
        habitacionRepository.save(hab);

        // anular facturas pendientes (ABIERTA o VENCIDA)
        List<Factura> facturasPendientes = facturaRepository.findByContratoId(contratoId);
        int facturasAnuladas = 0;
        for (Factura factura : facturasPendientes) {
            if (factura.getEstado() == Factura.EstadoFactura.ABIERTA || 
                factura.getEstado() == Factura.EstadoFactura.VENCIDA) {
                factura.setEstado(Factura.EstadoFactura.CANCELADA);
                facturaRepository.save(factura);
                facturasAnuladas++;
            }
        }
        log.info("Se anularon {} facturas pendientes del contrato ID: {}", facturasAnuladas, contratoId);

        return mapper.toDetailDto(contrato);
    }

    /*
     * =======================================================
     * SUBIR FIRMA DIGITAL
     * =======================================================
     */
    @Override
    public ContratoDetailResponseDto uploadFirma(Long propiedadId, Long contratoId, MultipartFile file)
            throws IOException {
        log.info("Subiendo firma para contrato ID: {}", contratoId);

        Propiedad propiedad = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada."));
        securityHelper.validateOwner(propiedad);

        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado."));

        if (!Objects.equals(contrato.getPropiedad().getId(), propiedadId)) {
            throw new BusinessException("Este contrato no pertenece a esta propiedad.");
        }

        if (file.isEmpty()) {
            throw new BusinessException("El archivo de firma está vacío.");
        }

        String path = fileStorageService.saveFile(file, "firmas");
        contrato.setFirmaPath(path);
        contratoRepository.save(contrato);

        // Generar facturas SOLO al registrar la firma del contrato
        facturaService.generarFacturasParaContrato(contrato);

        return mapper.toDetailDto(contrato);
    }

    /*
     * =======================================================
     * OBTENER FIRMA DIGITAL
     * =======================================================
     */
    @Override
    @Transactional(readOnly = true)
    public byte[] getFirma(Long propiedadId, Long contratoId) {
        log.info("📝 Solicitando firma para contrato ID: {} de propiedad ID: {}", contratoId, propiedadId);

        Propiedad propiedad = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada."));
        securityHelper.validateOwner(propiedad);

        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado."));

        if (!Objects.equals(contrato.getPropiedad().getId(), propiedadId)) {
            throw new BusinessException("Este contrato no pertenece a esta propiedad.");
        }

        if (contrato.getFirmaPath() == null) {
            log.warn("⚠️ Contrato ID: {} no tiene firma registrada", contratoId);
            throw new BusinessException("Este contrato no tiene firma.");
        }

        log.info("📁 Ruta de firma: {}", contrato.getFirmaPath());

        try {
            byte[] firmaBytes = fileStorageService.loadFile(contrato.getFirmaPath());
            log.info("✅ Firma cargada exitosamente. Tamaño: {} bytes", firmaBytes.length);
            return firmaBytes;
        } catch (IOException e) {
            log.error("❌ Error al leer el archivo de firma: {}", contrato.getFirmaPath(), e);
            throw new RuntimeException("Error al leer el archivo de firma", e);
        }
    }
}
