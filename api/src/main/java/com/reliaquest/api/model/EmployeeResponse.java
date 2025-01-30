package com.reliaquest.api.model;

import com.reliaquest.api.dto.EmployeeData;
import java.util.List;
import lombok.Data;

@Data
public class EmployeeResponse {
    private List<EmployeeData> data;
    private String status;
}
