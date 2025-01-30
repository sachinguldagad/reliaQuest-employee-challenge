package com.reliaquest.api.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.reliaquest.api.dto.EmployeeData;
import com.reliaquest.api.dto.EmployeeDto;
import com.reliaquest.api.service.EmployeeService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(SpringExtension.class)
public class EmployeeControllerTest {
    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();
    }

    @Test
    public void testGetAllEmployees() throws Exception {
        // Prepare
        EmployeeDto employee1 = EmployeeDto.builder()
                .data(EmployeeData.builder()
                        .id(UUID.randomUUID())
                        .name("Jane Doe")
                        .salary(15000)
                        .age(28)
                        .title("Manager")
                        .email("jane@example.com")
                        .build())
                .status("success")
                .build();
        EmployeeDto employee2 = EmployeeDto.builder()
                .data(EmployeeData.builder()
                        .id(UUID.randomUUID())
                        .name("John Smith")
                        .salary(12000)
                        .age(35)
                        .title("Engineer")
                        .email("john@example.com")
                        .build())
                .status("success")
                .build();

        // Mock
        when(employeeService.getAllEmployees()).thenReturn(List.of(employee1, employee2));

        // Perform
        mockMvc.perform(get("/api/v1/employees")).andExpect(status().isOk());

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    public void testGetEmployeeById() throws Exception {

        UUID employeeId = UUID.randomUUID();
        EmployeeDto employee = EmployeeDto.builder()
                .data(EmployeeData.builder()
                        .id(employeeId)
                        .name("Jane Doe")
                        .salary(15000)
                        .age(28)
                        .title("Manager")
                        .email("jane@example.com")
                        .build())
                .status("success")
                .build();

        // Mock
        when(employeeService.getEmployeeById("1")).thenReturn(employee);

        // Perform
        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(employeeId.toString()));

        verify(employeeService, times(1)).getEmployeeById("1");
    }

    @Test
    public void testDeleteEmployeeById() throws Exception {
        // Prepare test data
        String employeeId = "1";
        String message = "Employee deleted successfully";

        // Mock
        when(employeeService.deleteEmployeeById(employeeId)).thenReturn(message);

        // Perform
        mockMvc.perform(delete("/api/v1/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(message));

        verify(employeeService, times(1)).deleteEmployeeById(employeeId);
    }

    @Test
    public void testGetEmployeesByNameSearch() throws Exception {
        // Prepare
        UUID janeEmployeeId = UUID.randomUUID();
        UUID johnEmployeeId = UUID.randomUUID();
        EmployeeDto employee1 = EmployeeDto.builder()
                .data(EmployeeData.builder()
                        .id(janeEmployeeId)
                        .name("Jane Doe")
                        .salary(15000)
                        .age(28)
                        .title("Manager")
                        .email("jane@example.com")
                        .build())
                .status("success")
                .build();
        EmployeeDto employee2 = EmployeeDto.builder()
                .data(EmployeeData.builder()
                        .id(johnEmployeeId)
                        .name("John Smith")
                        .salary(12000)
                        .age(35)
                        .title("Engineer")
                        .email("john@example.com")
                        .build())
                .status("success")
                .build();

        // Mock
        when(employeeService.getEmployeesByNameSearch("Doe")).thenReturn(List.of(employee1, employee2));

        // Perform
        mockMvc.perform(get("/api/v1/employees/search/Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].data.id").value(janeEmployeeId.toString()))
                .andExpect(jsonPath("$[1].data.id").value(johnEmployeeId.toString()));

        verify(employeeService, times(1)).getEmployeesByNameSearch("Doe");
    }

    @Test
    public void testGetHighestSalaryOfEmployees() throws Exception {
        // Mock
        when(employeeService.getHighestSalaryOfEmployees()).thenReturn(6000);

        // Perform
        mockMvc.perform(get("/api/v1/employees/highest-salary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(6000));

        verify(employeeService, times(1)).getHighestSalaryOfEmployees();
    }

    @Test
    public void testGetTopTenHighestEarningEmployeeNames() throws Exception {

        when(employeeService.getTop10HighestEarningEmployeeNames()).thenReturn(List.of("John Doe", "Jane Smith"));

        mockMvc.perform(get("/api/v1/employees/top-10-earning"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("John Doe"))
                .andExpect(jsonPath("$[1]").value("Jane Smith"));

        verify(employeeService, times(1)).getTop10HighestEarningEmployeeNames();
    }
}
