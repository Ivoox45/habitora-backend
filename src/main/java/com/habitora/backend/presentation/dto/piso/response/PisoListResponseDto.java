package com.habitora.backend.presentation.dto.piso.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PisoListResponseDto {

    private Long id;
    private Integer numeroPiso;
}
