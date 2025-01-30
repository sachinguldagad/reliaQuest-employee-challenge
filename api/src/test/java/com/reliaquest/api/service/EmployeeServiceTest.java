package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.Exception.EmployeeCreationFailedException;
import com.reliaquest.api.Exception.EmployeeDataNotFoundException;
import com.reliaquest.api.dto.*;
import com.reliaquest.api.model.EmployeeResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class EmployeeServiceTest {

    @Mock
    private WebClient webClient;

    @InjectMocks
    private EmployeeService employeeService;

    @Mock
    private EmployeeService employeeServiceMock;

    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    private WebClient.ResponseSpec responseSpec;

    private final String baseUrl = "http://example.com/employees";

    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    private WebClient.RequestBodySpec requestBodySpec;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);

        webClient = mock(WebClient.class);
        requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        requestBodySpec = mock(WebClient.RequestBodySpec.class);

        employeeService = new EmployeeService(webClient);
    }

    @Test
    void testGetAllEmployees() {
        // Arrange
        EmployeeResponse employeeResponse = new EmployeeResponse();
        List<EmployeeData> employeeList = new ArrayList<>();

        employeeList.add(EmployeeData.builder()
                .id(UUID.randomUUID())
                .name("John Doe")
                .salary(10000)
                .age(30)
                .title("Developer")
                .email("john@example.com")
                .build());
        employeeResponse.setData(employeeList);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(EmployeeResponse.class)).thenReturn(Mono.just(employeeResponse));

        // Act
        List<EmployeeDto> employees = employeeService.getAllEmployees();

        // Assert
        assertNotNull(employees);
        assertEquals(1, employees.size());
        assertEquals("John Doe", employees.get(0).getData().getName());
    }

    @Test
    void testGetAllEmployeesNoData() {
        // Arrange
        EmployeeResponse employeeResponse = new EmployeeResponse();
        employeeResponse.setData(new ArrayList<>());

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(EmployeeResponse.class)).thenReturn(Mono.just(employeeResponse));

        // Act & Assert
        assertThrows(EmployeeDataNotFoundException.class, () -> employeeService.getAllEmployees());
    }

    @ParameterizedTest
    @ValueSource(strings = {"e1d1-8fbf", "f391-bc92"})
    void testGetEmployeeById(String id) {
        // Arrange
        EmployeeDto employeeDto = EmployeeDto.builder()
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

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(EmployeeDto.class)).thenReturn(Mono.just(employeeDto));

        EmployeeDto result = employeeService.getEmployeeById(id);

        // Assert: Verify that the result is as expected
        assertNotNull(result);
        assertEquals("Jane Doe", result.getData().getName());
    }

    @Test
    void testCreateEmployeeSuccess() {
        // Arrange: Mock the WebClient call for success
        CreateEmployeeDto createEmployeeDto = new CreateEmployeeDto();
        createEmployeeDto.setName("John Smith");
        createEmployeeDto.setSalary(12000);
        createEmployeeDto.setAge(35);
        createEmployeeDto.setTitle("Engineer");

        EmployeeDto employeeDtoResponse = EmployeeDto.builder()
                .data(EmployeeData.builder()
                        .id(UUID.randomUUID())
                        .name("John Smith")
                        .salary(12000)
                        .age(35)
                        .title("Engineer")
                        .email("john.smith@example.com")
                        .build())
                .status("created")
                .build();

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(createEmployeeDto)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(EmployeeDto.class)).thenReturn(Mono.just(employeeDtoResponse));

        // Act: Call the method under test
        EmployeeDto result = employeeService.createEmployee(createEmployeeDto);

        // Assert: Check that the response is as expected
        assertNotNull(result);
        assertEquals("John Smith", result.getData().getName());
        assertEquals(12000, result.getData().getSalary());
        assertEquals(35, result.getData().getAge());
        assertEquals("Engineer", result.getData().getTitle());

        // Verify the WebClient interactions
        verify(webClient).post();
        verify(requestBodyUriSpec).uri(anyString());
        verify(requestBodySpec).bodyValue(createEmployeeDto);
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(EmployeeDto.class);
    }

    @Test
    void testCreateEmployeeFailNoResponse() {
        // Arrange
        CreateEmployeeDto createEmployeeDto = new CreateEmployeeDto();
        createEmployeeDto.setName("John Smith");
        createEmployeeDto.setSalary(12000);
        createEmployeeDto.setAge(35);
        createEmployeeDto.setTitle("Engineer");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(createEmployeeDto)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(EmployeeDto.class)).thenReturn(Mono.empty());

        // Act & Assert
        assertThrows(EmployeeCreationFailedException.class, () -> employeeService.createEmployee(createEmployeeDto));

        // Verify WebClient interactions
        verify(webClient).post();
        verify(requestBodyUriSpec).uri(anyString());
        verify(requestBodySpec).bodyValue(createEmployeeDto);
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(EmployeeDto.class);
    }

    @Test
    void testCreateEmployeeRateLimitFallback() {
        // Arrange
        CreateEmployeeDto createEmployeeDto = new CreateEmployeeDto();
        createEmployeeDto.setName("John Smith");
        createEmployeeDto.setSalary(12000);
        createEmployeeDto.setAge(35);
        createEmployeeDto.setTitle("Engineer");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(createEmployeeDto)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(EmployeeDto.class))
                .thenReturn(Mono.error(new RuntimeException("Rate limit exceeded")));

        // Act
        assertThrows(RuntimeException.class, () -> employeeService.createEmployee(createEmployeeDto));

        // Verify WebClient interactions
        verify(webClient).post();
        verify(requestBodyUriSpec).uri(anyString());
        verify(requestBodySpec).bodyValue(createEmployeeDto);
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(EmployeeDto.class);
    }

    @Test
    void testGetEmployeesByNameSearch_Success() {
        // Arrange
        EmployeeDto employee1 = EmployeeDto.builder()
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

        EmployeeDto employee2 = EmployeeDto.builder()
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

        List<EmployeeDto> mockEmployeeList = Arrays.asList(employee1, employee2);

        // Mock
        when(employeeServiceMock.getAllEmployees()).thenReturn(mockEmployeeList);

        // Act
        List<EmployeeDto> result = employeeServiceMock.getEmployeesByNameSearch("Jane Doe");

        // Assert
        assertNotNull(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"smith", "Jane", "jth", "joe smith"})
    void testGetEmployeesByNameSearch_CaseInsensitiveSearch(String name) {
        // Arrange
        EmployeeDto employee1 = EmployeeDto.builder()
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

        EmployeeDto employee2 = EmployeeDto.builder()
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

        List<EmployeeDto> mockEmployeeList = Arrays.asList(employee1, employee2);

        when(employeeServiceMock.getAllEmployees()).thenReturn(mockEmployeeList);

        List<EmployeeDto> result = employeeServiceMock.getEmployeesByNameSearch(name);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetTop10HighestEarningEmployeeNames_Success() {
        // Mock data
        EmployeeResponse mockResponse = new EmployeeResponse();
        mockResponse.setData(Arrays.asList(
                EmployeeData.builder()
                        .id(UUID.randomUUID())
                        .name("John Doe")
                        .salary(1000)
                        .age(30)
                        .title("Software Engineer")
                        .email("johndoe@example.com")
                        .build(),
                EmployeeData.builder()
                        .id(UUID.randomUUID())
                        .name("Jane Smith")
                        .salary(3000)
                        .age(28)
                        .title("Product Manager")
                        .email("janesmith@example.com")
                        .build(),
                EmployeeData.builder()
                        .id(UUID.randomUUID())
                        .name("Alex Johnson")
                        .salary(2000)
                        .age(35)
                        .title("QA Engineer")
                        .email("alexjohnson@example.com")
                        .build(),
                EmployeeData.builder()
                        .id(UUID.randomUUID())
                        .name("Emily Davis")
                        .salary(4000)
                        .age(40)
                        .title("Tech Lead")
                        .email("emilydavis@example.com")
                        .build()));

        mockWebClient(mockResponse);

        // Act
        List<String> result = employeeService.getTop10HighestEarningEmployeeNames();

        // Assert
        assertEquals(4, result.size());
        assertEquals("Emily Davis", result.get(0)); // Highest salary
        assertEquals("Jane Smith", result.get(1));
        assertEquals("Alex Johnson", result.get(2));
        assertEquals("John Doe", result.get(3)); // Lowest salary
    }

    @Test
    void testGetTop10HighestEarningEmployeeNames_EmptyData() {
        // Mock empty employee data
        EmployeeResponse mockResponse = new EmployeeResponse();
        mockResponse.setData(null);

        mockWebClient(mockResponse);

        // Act & Assert
        assertThrows(EmployeeDataNotFoundException.class, () -> {
            employeeService.getTop10HighestEarningEmployeeNames();
        });
    }

    private void mockWebClient(EmployeeResponse mockResponse) {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(EmployeeResponse.class)).thenReturn(Mono.just(mockResponse));
    }
}
