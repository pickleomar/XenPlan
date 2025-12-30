# Testing Guide for XenPlan Business Logic

This guide explains how to test the business logic of the XenPlan application.

## Test Structure

The test suite is organized into three types:

1. **Unit Tests** - Test individual service methods with mocked dependencies
2. **Integration Tests** - Test full flows with real database (H2 in-memory)
3. **Context Tests** - Verify Spring Boot application context loads correctly

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=ReservationServiceTest
```

### Run Tests with Coverage
```bash
mvn test jacoco:report
```

## Test Files

### Unit Tests

#### `ReservationServiceTest.java`
Tests the `ReservationService` business logic:
- ✅ Create reservation for PUBLISHED event
- ✅ Reject reservation for DRAFT event
- ✅ Reject reservation for FINISHED event
- ✅ Validate seat limits (1-10)
- ✅ Validate capacity limits
- ✅ Cancel reservation (>48 hours rule)
- ✅ Authorization checks
- ✅ Auto-calculation of total amount
- ✅ Auto-generation of reservation codes

#### `EventServiceTest.java`
Tests the `EventService` business logic:
- ✅ Create event (ADMIN/ORGANIZER only)
- ✅ Reject event creation by CLIENT
- ✅ Validate date constraints (future start, end after start)
- ✅ Publish DRAFT events
- ✅ Delete events (only if no reservations)
- ✅ Calculate available seats
- ✅ Authorization checks

### Integration Tests

#### `ReservationServiceIntegrationTest.java`
Tests full database integration:
- ✅ Create and persist reservations
- ✅ Multiple reservations affect capacity
- ✅ Retrieve reservations by user
- ✅ Cancel reservations
- ✅ Verify reservations by code

## Test Configuration

Tests use:
- **H2 in-memory database** (configured in `application-test.properties`)
- **JPA DDL auto-create** (Liquibase disabled for tests)
- **Mockito** for unit test mocking
- **Spring Data JPA Test** for integration tests

## Key Business Rules Tested

### Reservation Rules
1. ✅ Event must be PUBLISHED
2. ✅ Event must not be FINISHED
3. ✅ Seats must be 1-10
4. ✅ Cannot exceed available capacity
5. ✅ Cancel only if >48 hours before event
6. ✅ Auto-calculate total amount
7. ✅ Auto-generate unique reservation codes

### Event Rules
1. ✅ Only ADMIN/ORGANIZER can create
2. ✅ Start date must be in future
3. ✅ End date after start date
4. ✅ Cannot update PUBLISHED/FINISHED
5. ✅ Cannot delete if reservations exist
6. ✅ Only creator/ADMIN can modify

## Writing New Tests

### Unit Test Template
```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {
    @Mock
    private MyRepository repository;
    
    @InjectMocks
    private MyServiceImpl service;
    
    @Test
    void testBusinessRule() {
        // Given
        when(repository.findById(any())).thenReturn(Optional.of(entity));
        
        // When
        Result result = service.doSomething();
        
        // Then
        assertNotNull(result);
        verify(repository).save(any());
    }
}
```

### Integration Test Template
```java
@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.liquibase.enabled=false"
})
@Import({MyServiceImpl.class})
class MyServiceIntegrationTest {
    @Autowired
    private MyService service;
    
    @Autowired
    private MyRepository repository;
    
    @Test
    void testFullFlow() {
        // Test with real database
    }
}
```

## Common Test Scenarios

### Testing Business Rule Violations
```java
@Test
void testBusinessRuleViolation() {
    // Given
    when(repository.findById(any())).thenReturn(Optional.of(invalidEntity));
    
    // When/Then
    ConflictException exception = assertThrows(ConflictException.class, () -> {
        service.doSomething();
    });
    
    assertEquals("Expected error message", exception.getMessage());
}
```

### Testing Authorization
```java
@Test
void testUnauthorizedAccess() {
    // Given
    User unauthorizedUser = createUser(Role.CLIENT);
    
    // When/Then
    ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
        service.restrictedAction(id, unauthorizedUser);
    });
}
```

## Debugging Tests

### Enable SQL Logging
Add to `application-test.properties`:
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### Run Single Test Method
```bash
mvn test -Dtest=ReservationServiceTest#testCreateReservation
```

### View Test Output
```bash
mvn test -X  # Verbose output
```

## Best Practices

1. ✅ **Test business rules, not implementation details**
2. ✅ **Use descriptive test names** (`@DisplayName`)
3. ✅ **Arrange-Act-Assert pattern** (Given-When-Then)
4. ✅ **One assertion per test** (when possible)
5. ✅ **Test both success and failure paths**
6. ✅ **Mock external dependencies** in unit tests
7. ✅ **Use real database** in integration tests

## Coverage Goals

- **Service Layer**: 80%+ coverage
- **Business Rules**: 100% coverage
- **Exception Handling**: All custom exceptions tested

## Troubleshooting

### Tests Fail with "Table not found"
- Ensure `spring.liquibase.enabled=false` in test properties
- Check `spring.jpa.hibernate.ddl-auto=create-drop`

### Mock Not Working
- Verify `@ExtendWith(MockitoExtension.class)`
- Check `@Mock` and `@InjectMocks` annotations

### Database State Issues
- Use `@BeforeEach` to clean database
- Consider `@Transactional` for test isolation

