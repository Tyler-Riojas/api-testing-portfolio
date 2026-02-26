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

### Quick Reference

| Command | Description |
|---------|-------------|
| `mvn test` | Run all tests |
| `mvn test -Psmoke` | Quick smoke tests (health + basic CRUD) |
| `mvn test -Pcrud` | Full CRUD operation tests |
| `mvn test -Pschema` | JSON schema validation tests |

### All Tests
```bash
# Run complete test suite
mvn test

# Run with verbose output
mvn test -X
```

### By Profile (Recommended)

Profiles provide pre-configured test groups with proper reporting:

```bash
mvn test -Psmoke    # Quick verification
mvn test -Pcrud     # Full CRUD operations
mvn test -Pschema   # Contract validation
```

---

#### Smoke Tests (`-Psmoke`)

**What it does:** Runs a small set of critical tests to verify the API is working.

**Tests included:**
- Health check (Is the API responding?)
- Get all bookings (Can we retrieve data?)
- Create a booking (Can we write data?)
- Validate booking list schema (Is the response format correct?)

**When to use:**
- After deploying to a new environment
- As a quick sanity check before running full test suite
- In CI/CD pipelines for fast feedback (runs in ~5 seconds)

**Why it's important:**
Smoke tests answer one question: *"Is the system basically working?"* If smoke tests fail, there's no point running the full suite. They catch major issues early and save time.

```bash
mvn test -Psmoke
```

---

#### CRUD Tests (`-Pcrud`)

**What it does:** Tests all Create, Read, Update, and Delete operations - the core functionality of any REST API.

**Tests included:**
- **Create (POST):** Create new bookings with full and minimal data
- **Read (GET):** Retrieve all bookings, single booking, filtered results, handle missing resources
- **Update (PUT):** Replace entire booking with new data
- **Update (PATCH):** Modify specific fields without affecting others
- **Delete (DELETE):** Remove booking and verify it's gone

**When to use:**
- Testing new API endpoints
- Regression testing after code changes
- Verifying database operations work correctly

**Why it's important:**
CRUD operations are the backbone of REST APIs. If users can't create, read, update, or delete data, the API is broken. These tests verify the complete data lifecycle works as expected.

```bash
mvn test -Pcrud
```

---

#### Schema Tests (`-Pschema`)

**What it does:** Validates that API responses match a predefined JSON structure (contract testing).

**Tests included:**
- Booking list response matches expected array format
- Single booking response has all required fields
- Create booking response includes booking ID
- Auth token response has correct structure

**When to use:**
- After API changes to ensure backwards compatibility
- When integrating with external APIs
- To catch breaking changes before they reach production

**Why it's important:**
APIs are contracts between systems. If the response format changes unexpectedly (missing fields, wrong data types), client applications break. Schema tests ensure the API keeps its promises.

```bash
mvn test -Pschema
```

---

### By Test Group

Run tests by their `@Test(groups = {...})` annotation:

```bash
# By operation type
mvn test -Dgroups=get           # All GET requests
mvn test -Dgroups=post          # All POST requests
mvn test -Dgroups=put           # All PUT requests
mvn test -Dgroups=patch         # All PATCH requests
mvn test -Dgroups=delete        # All DELETE requests

# By test category
mvn test -Dgroups=smoke         # Quick verification tests
mvn test -Dgroups=crud          # CRUD operations
mvn test -Dgroups=schema        # Schema validation
mvn test -Dgroups=health        # Health check only
mvn test -Dgroups=auth          # Authentication tests
mvn test -Dgroups=negative      # Negative/error case tests
mvn test -Dgroups=validation    # Input validation tests

# Combine multiple groups (OR logic)
mvn test -Dgroups="smoke,health"
mvn test -Dgroups="get,post"
```

### By Test Class

```bash
# Run all tests in a specific class
mvn test -Dtest=BookingApiTest
mvn test -Dtest=SchemaValidationTest

# Run multiple test classes
mvn test -Dtest=BookingApiTest,SchemaValidationTest
```

### By Test Method

```bash
# Run a specific test method
mvn test -Dtest=BookingApiTest#testCreateBooking
mvn test -Dtest=BookingApiTest#testHealthCheck
mvn test -Dtest=SchemaValidationTest#testBookingListSchema

# Run multiple specific methods
mvn test -Dtest=BookingApiTest#testCreateBooking+testGetAllBookings

# Pattern matching (wildcard)
mvn test -Dtest=BookingApiTest#test*Booking
mvn test -Dtest=*Test#testHealth*
```

### Environment Configuration

```bash
# Use a different environment config file
mvn test -Denv=staging              # Uses config/staging.properties
mvn test -Denv=prod                 # Uses config/prod.properties

# Override specific properties
mvn test -Dapi.base.url=https://custom-api.com
mvn test -Dapi.timeout=60
mvn test -Dapi.auth.username=testuser -Dapi.auth.password=testpass

# Combine environment and overrides
mvn test -Psmoke -Denv=staging -Dapi.timeout=30
```

### CI/CD Pipeline Examples

```bash
# Jenkins / GitHub Actions - Smoke tests with custom reporting
mvn test -Psmoke -DreportName=CI_Smoke_Report

# Run all tests, fail fast on first failure
mvn test -Dsurefire.skipAfterFailureCount=1

# Generate test report without running tests (if results exist)
mvn surefire-report:report-only

# Clean and run fresh
mvn clean test -Psmoke
```

### View Test Reports

```bash
# macOS
open reports/API_Test_Report_*.html

# Linux
xdg-open reports/API_Test_Report_*.html

# Windows
start reports\API_Test_Report_*.html
```

---

## Test Coverage

### BookingApiTest.java

CRUD operations demonstrating the full booking lifecycle:

| Test | HTTP | Description | Groups |
|------|------|-------------|--------|
| `testHealthCheck` | GET | Verify API availability | smoke, health |
| `testGetAllBookings` | GET | Retrieve all booking IDs | smoke, crud, get |
| `testGetBookingsWithFilter` | GET | Filter by guest name | crud, get |
| `testGetBookingById` | GET | Get specific booking details | crud, get |
| `testGetNonExistentBooking` | GET | Handle 404 response gracefully | crud, get, negative |
| `testCreateBooking` | POST | Create new booking with all fields | smoke, crud, post |
| `testCreateBookingMinimalFields` | POST | Validate minimal required payload | crud, post, validation |
| `testUpdateBooking` | PUT | Full update (replace entire resource) | crud, put |
| `testPartialUpdateBooking` | PATCH | Partial update (modify specific fields) | crud, patch |
| `testDeleteBooking` | DELETE | Delete booking and verify removal | crud, delete |

### SchemaValidationTest.java

Contract testing to ensure API responses match expected structure:

| Test | Description | Groups |
|------|-------------|--------|
| `testBookingListSchema` | Validate list response against JSON schema | schema, smoke |
| `testBookingDetailSchema` | Validate single booking response structure | schema |
| `testBookingCreationSchema` | Validate create response includes booking ID | schema |
| `testAuthTokenSchema` | Validate auth token response format | schema, auth |

### Available Test Groups

| Group | Purpose | Example Command |
|-------|---------|-----------------|
| `smoke` | Quick verification tests | `mvn test -Dgroups=smoke` |
| `crud` | All CRUD operations | `mvn test -Dgroups=crud` |
| `schema` | JSON schema validation | `mvn test -Dgroups=schema` |
| `get` | GET request tests | `mvn test -Dgroups=get` |
| `post` | POST request tests | `mvn test -Dgroups=post` |
| `put` | PUT request tests | `mvn test -Dgroups=put` |
| `patch` | PATCH request tests | `mvn test -Dgroups=patch` |
| `delete` | DELETE request tests | `mvn test -Dgroups=delete` |
| `health` | Health check only | `mvn test -Dgroups=health` |
| `auth` | Authentication tests | `mvn test -Dgroups=auth` |
| `negative` | Error handling tests | `mvn test -Dgroups=negative` |
| `validation` | Input validation tests | `mvn test -Dgroups=validation` |

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
