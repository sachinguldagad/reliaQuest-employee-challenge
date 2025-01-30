package com.reliaquest.api.mapper;

import com.reliaquest.api.dto.EmployeeData;
import com.reliaquest.api.dto.EmployeeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EmployeeMapper {
    EmployeeMapper INSTANCE = Mappers.getMapper(EmployeeMapper.class);

    @Mapping(source = "status", target = "status") // If status comes from EmployeeResponse
    EmployeeDto employeeDataToEmployeeDto(EmployeeData data, String status);
}
