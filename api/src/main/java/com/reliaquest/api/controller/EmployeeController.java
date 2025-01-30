package com.reliaquest.api.controller;

import com.reliaquest.api.dto.CreateEmployeeDto;
import com.reliaquest.api.dto.EmployeeDto;
import com.reliaquest.api.service.EmployeeService;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController implements IEmployeeController<EmployeeDto, CreateEmployeeDto> {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
        List<EmployeeDto> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable String id) {
        EmployeeDto employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/search/{searchString}")
    public ResponseEntity<List<EmployeeDto>> getEmployeesByNameSearch(@PathVariable String searchString) {
        List<EmployeeDto> employees = employeeService.getEmployeesByNameSearch(searchString);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/highest-salary")
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        int highestSalary = employeeService.getHighestSalaryOfEmployees();
        return ResponseEntity.ok(highestSalary);
    }

    @GetMapping("/top-10-earning")
    @RateLimiter(name = "employeeService", fallbackMethod = "getTop10HighestEarningEmployeeNamesFallback")
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        List<String> topEmployees = employeeService.getTop10HighestEarningEmployeeNames();
        return ResponseEntity.ok(topEmployees);
    }

    public ResponseEntity<List<String>> getTop10HighestEarningEmployeeNamesFallback(RequestNotPermitted ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(List.of(
                        "Rate limit exceeded for getting top10HighestEarningEmployeeNames. Please try again later."));
    }

    @PostMapping
    public ResponseEntity<EmployeeDto> createEmployee(@RequestBody CreateEmployeeDto createEmployeeDTO) {
        EmployeeDto createdEmployee = employeeService.createEmployee(createEmployeeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployeeById(@PathVariable String id) {
        String message = employeeService.deleteEmployeeById(id);
        return ResponseEntity.ok(message);
    }
}
