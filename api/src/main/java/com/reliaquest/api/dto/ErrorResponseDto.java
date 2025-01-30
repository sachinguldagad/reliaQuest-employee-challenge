package com.reliaquest.api.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ErrorResponseDto {
    private String errorCode;
    private String message;
}
