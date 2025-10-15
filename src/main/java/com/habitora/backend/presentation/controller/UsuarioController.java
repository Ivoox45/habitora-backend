package com.habitora.backend.presentation.controller;

import com.habitora.backend.presentation.dto.usuario.request.UsuarioCreateRequestDto;
import com.habitora.backend.presentation.dto.usuario.request.UsuarioUpdateRequestDto;
import com.habitora.backend.presentation.dto.usuario.response.UsuarioResponseDto;
import com.habitora.backend.presentation.dto.usuario.response.UsuarioListResponseDto;
import com.habitora.backend.service.interfaces.IUsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "APIs para gestionar usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final IUsuarioService usuarioService;

    @Operation(summary = "Crear usuario", description = "Crea un nuevo usuario. El email debe ser único.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario creado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content),
        @ApiResponse(responseCode = "409", description = "Email ya existe", content = @Content)
    })
    @PostMapping
    public ResponseEntity<UsuarioResponseDto> create(@Valid @RequestBody UsuarioCreateRequestDto request) {
        UsuarioResponseDto response = usuarioService.create(request);
        return ResponseEntity.created(URI.create("/api/usuarios/" + response.getId())).body(response);
    }

    @Operation(summary = "Listar usuarios", description = "Obtiene todos los usuarios registrados")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios",
        content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UsuarioListResponseDto.class))))
    @GetMapping
    public ResponseEntity<List<UsuarioListResponseDto>> findAll() {
        List<UsuarioListResponseDto> list = usuarioService.findAll();
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Obtener usuario por id", description = "Obtiene un usuario por su id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDto> findById(@PathVariable Long id) {
        return usuarioService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario actualizado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDto> update(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateRequestDto request) {
        UsuarioResponseDto updated = usuarioService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario por su id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario eliminado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        usuarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
