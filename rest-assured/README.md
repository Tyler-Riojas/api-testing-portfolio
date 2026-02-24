# REST API Testing Portfolio

A professional REST API testing framework demonstrating **RestAssured**, **TestNG**, and **Extent Reports** with rich request/response visualization.

---

## What This Demonstrates

| Skill | Implementation |
|-------|----------------|
| **REST API Testing** | Full CRUD operations (GET, POST, PUT, PATCH, DELETE) |
| **Request Chaining** | Tests with data dependencies (create → read → update → delete) |
| **Authentication** | Token-based auth with automatic caching |
| **Schema Validation** | JSON Schema validation for API contracts |
| **Fluent Assertions** | Chainable validation DSL |
| **Rich Reporting** | Postman-style request/response visualization |
| **Configuration** | Environment-based config with property resolution |
| **Clean Architecture** | Separation of concerns, reusable utilities |

---

## Quick Start

```bash
# Clone and navigate
git clone <repository-url>
cd rest-assured

# Run all tests
mvn test

# View report
open reports/API_Test_Report_*.html
```

---

## Project Structure

```
src/
├── main/java/
│   ├── api/                    # Core API utilities
│   │   ├── ApiClient.java      # REST client with auto-logging
│   │   ├── ApiReporter.java    # Extent Reports integration
│   │   ├── AuthService.java    # Token management
│   │   └── ResponseValidator.java  # Fluent assertions
│   ├── config/
│   │   └── ApiConfig.java      # Environment configuration
│   └── reporting/
│       ├── ExtentReportManager.java
│       └── ExtentTestListener.java
├── main/resources/
│   ├── config/
│   │   └── test.properties     # Default configuration
│   └── logback.xml
└── test/
    ├── java/tests/
    │   ├── BookingApiTest.java      # CRUD test suite
    │   └── SchemaValidationTest.java # Schema tests
    └── resources/
        ├── schemas/            # JSON schemas
        └── testng.xml
```

---

## Running Tests

### All Tests
```bash
mvn test
```

### By Group
```bash
# Smoke tests only
mvn test -Psmoke

# CRUD operations
mvn test -Pcrud

# Schema validation
mvn test -Pschema
```

### Specific Test
```bash
mvn test -Dtest=BookingApiTest
mvn test -Dtest=BookingApiTest#testCreateBooking
```

### Different Environment
```bash
mvn test -Denv=staging
mvn test -Dapi.base.url=https://custom-api.com
```

---

## Test Coverage

### BookingApiTest.java
| Test | Description | Groups |
|------|-------------|--------|
| `testHealthCheck` | Verify API availability | smoke, health |
| `testGetAllBookings` | Retrieve all booking IDs | smoke, crud |
| `testGetBookingsWithFilter` | Filter by guest name | crud |
| `testGetBookingById` | Get specific booking | crud |
| `testGetNonExistentBooking` | Handle 404 response | crud, negative |
| `testCreateBooking` | Create new booking | smoke, crud |
| `testCreateBookingMinimalFields` | Validate minimal payload | crud, validation |
| `testUpdateBooking` | Full update (PUT) | crud |
| `testPartialUpdateBooking` | Partial update (PATCH) | crud |
| `testDeleteBooking` | Delete and verify | crud |

### SchemaValidationTest.java
| Test | Description | Groups |
|------|-------------|--------|
| `testBookingListSchema` | Validate list response | schema, smoke |
| `testBookingDetailSchema` | Validate detail response | schema |
| `testBookingCreationSchema` | Validate create response | schema |
| `testAuthTokenSchema` | Validate auth response | schema, auth |

---

## Key Features

### Rich HTML Reports
Every API call is automatically logged with:
- Color-coded HTTP method badges (GET=blue, POST=green, PUT=orange, DELETE=red)
- Full request URL, headers, and body
- Response status code with semantic coloring
- Response timing in milliseconds
- Pretty-printed JSON bodies

### Fluent Validation
```java
new ResponseValidator(response)
    .statusCode(200)
    .hasJsonPath("bookingid")
    .jsonPathEquals("booking.firstname", "John")
    .responseTimeLessThan(2000)
    .matchesSchema("schemas/booking.json");
```

### Request Chaining
```java
// Create booking
Response createResponse = client.post("/booking", payload);
int bookingId = createResponse.jsonPath().getInt("bookingid");

// Update with auth
Response updateResponse = client
    .withHeaders(AuthService.getAuthHeaders())
    .put("/booking/" + bookingId, updatedPayload);
```

### Environment Configuration
```properties
# config/test.properties
api.base.url=https://restful-booker.herokuapp.com
api.timeout=30
api.auth.username=admin
api.auth.password=password123
```

Override with system properties:
```bash
mvn test -Dapi.base.url=https://staging-api.com
```

---

## Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 | Runtime |
| RestAssured | 6.0.0 | HTTP client |
| TestNG | 7.11.0 | Test framework |
| Jackson | 2.18.3 | JSON processing |
| Extent Reports | 5.1.2 | HTML reporting |
| AssertJ | 3.27.6 | Fluent assertions |
| SLF4J + Logback | 2.0.17 | Logging |

---

## Sample Report

The Extent Report shows each test with full API call details:

```
┌─────────────────────────────────────────────────────────────┐
│  POST  https://restful-booker.herokuapp.com/booking         │
├─────────────────────────────────────────────────────────────┤
│ Request Headers:                                            │
│   Content-Type: application/json                            │
│   Accept: application/json                                  │
│                                                             │
│ Request Body:                                               │
│ {                                                           │
│   "firstname": "Jane",                                      │
│   "lastname": "Doe",                                        │
│   "totalprice": 250,                                        │
│   "depositpaid": true                                       │
│ }                                                           │
├─────────────────────────────────────────────────────────────┤
│  200 OK                                              142 ms │
├─────────────────────────────────────────────────────────────┤
│ Response Body:                                              │
│ {                                                           │
│   "bookingid": 123,                                         │
│   "booking": {                                              │
│     "firstname": "Jane",                                    │
│     "lastname": "Doe",                                      │
│     ...                                                     │
│   }                                                         │
│ }                                                           │
└─────────────────────────────────────────────────────────────┘
```

---

## License

MIT License

---

*Built to demonstrate professional API testing practices*
