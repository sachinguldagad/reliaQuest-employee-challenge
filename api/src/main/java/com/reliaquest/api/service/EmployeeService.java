package com.reliaquest.api.service;

import com.reliaquest.api.Exception.*;
import com.reliaquest.api.dto.*;
import com.reliaquest.api.mapper.EmployeeMapper;
import com.reliaquest.api.model.EmployeeDeleteModel;
import com.reliaquest.api.model.EmployeeResponse;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static com.reliaquest.api.aop.LoggingAspect.logger;

@Service
@Slf4j
public class EmployeeService {

    private final WebClient webClient;
    private final String baseUrl = "http://localhost:8112/api/v1/employee";
    private final EmployeeMapper employeeMapper = EmployeeMapper.INSTANCE;

    public EmployeeService(WebClient webClient) {
        this.webClient = webClient;
    }

    @Retryable(
            retryFor = WebClientResponseException.TooManyRequests.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public List<EmployeeDto> getAllEmployees() {
        EmployeeResponse employeeResponse = webClient
                .get()
                .uri(baseUrl)
                .retrieve()
                .bodyToMono(EmployeeResponse.class)
                .block();
        if (employeeResponse == null
                || employeeResponse.getData() == null
                || employeeResponse.getData().isEmpty()) {
            throw new EmployeeDataNotFoundException("No employee data found.");
        }
        return employeeResponse.getData().stream()
                .map(employeeData ->
                        employeeMapper.employeeDataToEmployeeDto(employeeData, employeeResponse.getStatus()))
                .collect(Collectors.toList());
    }

    @Recover
    public List<EmployeeDto> getAllEmployeesFallback(WebClientResponseException.TooManyRequests e) {
        logger.error("Retry Exceeded. Try later");
        throw new HandleRetryException("Retry exceeded. Please try again later.");
    }

    @Retryable(
            retryFor = {WebClientResponseException.TooManyRequests.class, EmployeeNotFoundException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public EmployeeDto getEmployeeById(String id) {
            EmployeeDto employeeDto = webClient
                    .get()
                    .uri(baseUrl + "/" + id)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError(),
                            clientResponse ->
                                    Mono.error(new EmployeeNotFoundException("Employee not found with id: " + id)))
                    .bodyToMono(EmployeeDto.class)
                    .block();
            return employeeDto;

    }

    @Recover
    public EmployeeDto getEmployeeByIdFallback(WebClientResponseException.TooManyRequests e, String id) {
        logger.error("Retry Exceeded. Try later");
        throw new HandleRetryException("Retry exceeded for fetching employee by ID. Please try again later.");
    }

    @Recover
    public EmployeeDto getEmployeeByIdNotFound(EmployeeNotFoundException e, String id) {
        logger.error("Employee not found. Try later");
        throw new EmployeeNotFoundException("Employee not found with id: " + id);
    }

    @Retryable(
            retryFor = WebClientResponseException.TooManyRequests.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public List<EmployeeDto> getEmployeesByNameSearch(String name) {
        List<EmployeeDto> allEmployees = getAllEmployees();
        List<EmployeeDto> employeesWithName = allEmployees.stream()
                .filter(emp -> emp.getData().getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());

        if (employeesWithName.isEmpty()) {
            logger.error("Employee not found. Try later");
            throw new EmployeeDataNotFoundException("No employees found with the name: " + name);
        }
        return employeesWithName;
    }

    @Recover
    public List<EmployeeDto> getEmployeesByNameSearchFallback(
            WebClientResponseException.TooManyRequests e, String name) {
        logger.error("Retry Exceeded. Try later");
        throw new HandleRetryException("Retry exceeded for searching employees by name. Please try again later.");
    }

    @Retryable(
            retryFor = WebClientResponseException.TooManyRequests.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public int getHighestSalaryOfEmployees() {

        return getAllEmployees().stream()
                .mapToInt(emp -> emp.getData().getSalary())
                .max()
                .orElseThrow(() -> new EmployeeDataNotFoundException("Unable to determine highest salary."));
    }

    @Recover
    public int getHighestSalaryOfEmployeesFallback(WebClientResponseException.TooManyRequests e) {
        logger.error("Retry Exceeded. Try later");
        throw new HandleRetryException("Retry exceeded Please try again later.");
    }

    @Retryable(
            retryFor = WebClientResponseException.TooManyRequests.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public List<String> getTop10HighestEarningEmployeeNames() {
        EmployeeResponse employeeResponse = webClient
                .get()
                .uri(baseUrl)
                .retrieve()
                .bodyToMono(EmployeeResponse.class)
                .block();

        if (employeeResponse == null
                || employeeResponse.getData() == null
                || employeeResponse.getData().isEmpty()) {
            logger.error("Employee data not found. Try later");
            throw new EmployeeDataNotFoundException(
                    "No employees found to determine the top 10 highest earning employees.");
        }
        return employeeResponse.getData().stream()
                .sorted(Comparator.comparingInt(EmployeeData::getSalary).reversed())
                .limit(10)
                .map(EmployeeData::getName)
                .collect(Collectors.toList());
    }

    @Recover
    public List<String> recoverGetTop10HighestEarningEmployeeNames(WebClientResponseException.TooManyRequests e) {
        logger.error("Retry Exceeded. Try later");
        throw new HandleRetryException(
                "Retry exceeded for getting top 10 Highest earning employee. Please try again later.");
    }

    @Retryable(
            retryFor = WebClientResponseException.TooManyRequests.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public EmployeeDto createEmployee(CreateEmployeeDto createEmployeeDTO) {
        EmployeeDto createEmployeeResponse = webClient
                .post()
                .uri(baseUrl)
                .bodyValue(createEmployeeDTO)
                .retrieve()
                .bodyToMono(EmployeeDto.class)
                .block();

        if (createEmployeeResponse == null) {
            logger.error("Employee creation failed. Try later");
            throw new EmployeeCreationFailedException("Employee creation failed. No response received.");
        }
        return createEmployeeResponse;
    }

    @Recover
    public EmployeeDto createEmployeeFallback(
            WebClientResponseException.TooManyRequests e, CreateEmployeeDto createEmployeeDTO) {
        logger.error("Retry Exceeded. Try later");
        throw new HandleRetryException("Retry exceeded for creating employee. Please try again later.");
    }

    @Retryable(
            retryFor = WebClientResponseException.TooManyRequests.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public String deleteEmployeeById(String id) {
        EmployeeDto employeeDto = getEmployeeById(id);
        DeleteEmployeeDto deleteEmployeeDto =
                new DeleteEmployeeDto(employeeDto.getData().getName());
        EmployeeDeleteModel employeeDeleteModel = webClient
                .method(HttpMethod.DELETE)
                .uri(baseUrl)
                .bodyValue(deleteEmployeeDto)
                .retrieve()
                .bodyToMono(EmployeeDeleteModel.class)
                .block();

        if (employeeDeleteModel == null) {
            logger.error("Employee delete failed. Try later");
            throw new EmployeeDeleteFailedException("Failed to delete employee with ID: " + id);
        }
        return employeeDeleteModel.getStatus();
    }

    @Recover
    public String deleteEmployeeByIdFallback(WebClientResponseException.TooManyRequests e, String id) {
        logger.error("Retry Exceeded. Try later");
        throw new HandleRetryException("Retry exceeded for deleting employee. Please try again later.");
    }
}
