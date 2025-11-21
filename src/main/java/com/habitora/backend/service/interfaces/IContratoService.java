package com.habitora.backend.service.interfaces;

import com.habitora.backend.persistence.entity.Contrato;
import com.habitora.backend.presentation.dto.contrato.request.ContratoCreateRequestDto;
import com.habitora.backend.presentation.dto.contrato.response.ContratoDetailResponseDto;
import com.habitora.backend.presentation.dto.contrato.response.ContratoListResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IContratoService {

    ContratoDetailResponseDto create(Long propiedadId, ContratoCreateRequestDto request);

    List<ContratoListResponseDto> list(Long propiedadId, Contrato.EstadoContrato estado, String search);

    ContratoDetailResponseDto getById(Long propiedadId, Long contratoId);

    ContratoDetailResponseDto finalizar(Long propiedadId, Long contratoId);

    ContratoDetailResponseDto uploadFirma(Long propiedadId, Long contratoId, MultipartFile file) throws IOException;

    byte[] getFirma(Long propiedadId, Long contratoId);
}
