package com.habitora.backend.service.implementation;

import com.habitora.backend.persistence.entity.Usuario;
import com.habitora.backend.persistence.repository.UsuarioRepository;
import com.habitora.backend.presentation.dto.usuario.request.UsuarioCreateRequestDto;
import com.habitora.backend.presentation.dto.usuario.request.UsuarioUpdateRequestDto;
import com.habitora.backend.presentation.dto.usuario.response.UsuarioListResponseDto;
import com.habitora.backend.presentation.dto.usuario.response.UsuarioResponseDto;
import com.habitora.backend.service.interfaces.IUsuarioService;
import com.habitora.backend.util.mapper.UsuarioMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }

    @Override
    @Transactional
    public UsuarioResponseDto create(UsuarioCreateRequestDto createDto) {
        Usuario entidad = usuarioMapper.toEntity(createDto);
        Usuario saved = usuarioRepository.save(entidad);
        return usuarioMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UsuarioResponseDto> findById(Long id) {
        return usuarioRepository.findById(id).map(usuarioMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioListResponseDto> findAll() {
        return usuarioRepository.findAll()
                .stream()
                .map(usuarioMapper::toListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UsuarioResponseDto update(Long id, UsuarioUpdateRequestDto updateDto) {
        return usuarioRepository.findById(id).map(existing -> {
            if (updateDto.getNombreCompleto() != null) existing.setNombreCompleto(updateDto.getNombreCompleto());
            if (updateDto.getEmail() != null) existing.setEmail(updateDto.getEmail());
            if (updateDto.getTelefonoWhatsapp() != null) existing.setTelefonoWhatsapp(updateDto.getTelefonoWhatsapp());
            if (updateDto.getContraseña() != null) existing.setContraseña(updateDto.getContraseña());
            Usuario saved = usuarioRepository.save(existing);
            return usuarioMapper.toResponse(saved);
        }).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + id));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }

}
