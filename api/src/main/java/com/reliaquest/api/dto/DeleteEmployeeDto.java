package com.reliaquest.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DeleteEmployeeDto {

    @NotBlank
    private String name;
}
