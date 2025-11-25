package com.habitora.backend.service.implementation;

import com.habitora.backend.persistence.entity.Usuario;
import com.habitora.backend.persistence.repository.PropiedadRepository;
import com.habitora.backend.persistence.repository.UsuarioRepository;
import com.habitora.backend.presentation.dto.usuario.request.UsuarioCreateRequestDto;
import com.habitora.backend.presentation.dto.usuario.request.UsuarioUpdateRequestDto;
import com.habitora.backend.presentation.dto.usuario.response.PropiedadSimpleDto;
import com.habitora.backend.presentation.dto.usuario.response.UsuarioListResponseDto;
import com.habitora.backend.presentation.dto.usuario.response.UsuarioPropiedadesDto;
import com.habitora.backend.presentation.dto.usuario.response.UsuarioResponseDto;
import com.habitora.backend.service.interfaces.IUsuarioService;
import com.habitora.backend.exception.ResourceNotFoundException;
import com.habitora.backend.util.mapper.UsuarioMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PropiedadRepository propiedadRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper,
            PropiedadRepository propiedadRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.propiedadRepository = propiedadRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UsuarioResponseDto create(UsuarioCreateRequestDto createDto) {
        log.info("Creando nuevo usuario con email: {}", createDto.getEmail());
        Usuario entidad = usuarioMapper.toEntity(createDto);
        entidad.setContrasena(passwordEncoder.encode(entidad.getContrasena()));
        Usuario saved = usuarioRepository.save(entidad);
        log.info("Usuario creado con ID: {}", saved.getId());
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
        log.info("Actualizando usuario con ID: {}", id);
        return usuarioRepository.findById(id).map(existing -> {
            if (updateDto.getNombreCompleto() != null)
                existing.setNombreCompleto(updateDto.getNombreCompleto());
            if (updateDto.getEmail() != null)
                existing.setEmail(updateDto.getEmail());
            if (updateDto.getTelefonoWhatsapp() != null)
                existing.setTelefonoWhatsapp(updateDto.getTelefonoWhatsapp());
            if (updateDto.getContrasena() != null)
                existing.setContrasena(passwordEncoder.encode(updateDto.getContrasena()));
            Usuario saved = usuarioRepository.save(existing);
            return usuarioMapper.toResponse(saved);
        }).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.info("Eliminando usuario con ID: {}", id);
        usuarioRepository.deleteById(id);
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Override
    public boolean userHasProperties(Long usuarioId) {
        return usuarioRepository.countPropiedadesByUsuarioId(usuarioId) > 0;
    }

    @Override
    public UsuarioPropiedadesDto getUserSimpleProperties(Long usuarioId) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<PropiedadSimpleDto> propiedades = propiedadRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(p -> new PropiedadSimpleDto(
                        p.getId(),
                        p.getNombre(),
                        p.getDireccion()))
                .toList();

        return new UsuarioPropiedadesDto(
                usuario.getId(),
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                propiedades);
    }

}
