package com.reliaquest.api.model;

import com.reliaquest.api.dto.EmployeeDto;
import lombok.Data;

@Data
public class CreateEmployeeResponse {

    private EmployeeDto data;
    private String status;
}
