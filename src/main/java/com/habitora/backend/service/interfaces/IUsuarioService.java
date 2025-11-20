package com.habitora.backend.service.interfaces;

import com.habitora.backend.persistence.entity.Usuario;
import com.habitora.backend.presentation.dto.usuario.request.UsuarioCreateRequestDto;
import com.habitora.backend.presentation.dto.usuario.request.UsuarioUpdateRequestDto;
import com.habitora.backend.presentation.dto.usuario.response.UsuarioListResponseDto;
import com.habitora.backend.presentation.dto.usuario.response.UsuarioResponseDto;

import java.util.List;
import java.util.Optional;

public interface IUsuarioService {

    UsuarioResponseDto create(UsuarioCreateRequestDto createDto);

    Optional<UsuarioResponseDto> findById(Long id);

    List<UsuarioListResponseDto> findAll();

    UsuarioResponseDto update(Long id, UsuarioUpdateRequestDto updateDto);

    void deleteById(Long id);

     Optional<Usuario> findByEmail(String email);

}
