package api;

import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Fluent response validator for API testing.
 *
 * <p>Provides chainable assertions for validating HTTP responses including:</p>
 * <ul>
 *   <li>Status codes and status ranges</li>
 *   <li>Response headers</li>
 *   <li>JSON path values and existence</li>
 *   <li>Response timing</li>
 *   <li>JSON schema validation</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <pre>
 * // Fluent validation chain
 * new ResponseValidator(response)
 *     .statusCode(200)
 *     .hasJsonPath("id")
 *     .jsonPathEquals("firstname", "John")
 *     .responseTimeLessThan(2000);
 *
 * // Static assertions
 * ResponseValidator.assertStatusCode(response, 201);
 * ResponseValidator.assertJsonSchema(response, "schemas/booking.json");
 * </pre>
 */
public class ResponseValidator {

    private static final Logger log = LoggerFactory.getLogger(lookup().lookupClass());

    private final Response response;

    /**
     * Create validator for a response.
     */
    public ResponseValidator(Response response) {
        this.response = response;
    }

    // ==================== Fluent Assertions ====================

    /**
     * Assert exact status code.
     */
    public ResponseValidator statusCode(int expectedCode) {
        assertStatusCode(response, expectedCode);
        return this;
    }

    /**
     * Assert status code is in success range (2xx).
     */
    public ResponseValidator isSuccess() {
        assertSuccess(response);
        return this;
    }

    /**
     * Assert status code is in client error range (4xx).
     */
    public ResponseValidator isClientError() {
        int code = response.getStatusCode();
        assertThat(code)
                .as("Status code should be client error (4xx)")
                .isBetween(400, 499);
        return this;
    }

    /**
     * Assert JSON path exists in response.
     */
    public ResponseValidator hasJsonPath(String jsonPath) {
        assertJsonPathExists(response, jsonPath);
        return this;
    }

    /**
     * Assert JSON path equals expected value.
     */
    public ResponseValidator jsonPathEquals(String jsonPath, Object expectedValue) {
        assertJsonPath(response, jsonPath, expectedValue);
        return this;
    }

    /**
     * Assert response time is below threshold.
     */
    public ResponseValidator responseTimeLessThan(long maxMillis) {
        assertResponseTime(response, maxMillis);
        return this;
    }

    /**
     * Assert header has expected value.
     */
    public ResponseValidator header(String name, String expectedValue) {
        assertHeader(response, name, expectedValue);
        return this;
    }

    /**
     * Assert response body contains text.
     */
    public ResponseValidator bodyContains(String text) {
        assertBodyContains(response, text);
        return this;
    }

    /**
     * Assert response matches JSON schema from classpath.
     */
    public ResponseValidator matchesSchema(String schemaPath) {
        assertJsonSchemaFromClasspath(response, schemaPath);
        return this;
    }

    /**
     * Get the underlying response.
     */
    public Response getResponse() {
        return response;
    }

    // ==================== Static Assertions ====================

    /**
     * Assert exact status code.
     */
    public static void assertStatusCode(Response response, int expectedCode) {
        int actual = response.getStatusCode();
        log.debug("Asserting status code: expected={}, actual={}", expectedCode, actual);
        assertThat(actual)
                .as("HTTP Status Code")
                .isEqualTo(expectedCode);
    }

    /**
     * Assert status code is in success range.
     */
    public static void assertSuccess(Response response) {
        int code = response.getStatusCode();
        log.debug("Asserting success status: {}", code);
        assertThat(code)
                .as("HTTP Status Code should be success (2xx)")
                .isBetween(200, 299);
    }

    /**
     * Assert JSON path exists.
     */
    public static void assertJsonPathExists(Response response, String jsonPath) {
        Object value = response.jsonPath().get(jsonPath);
        log.debug("Asserting JSON path exists: {} = {}", jsonPath, value);
        assertThat(value)
                .as("JSON path should exist: " + jsonPath)
                .isNotNull();
    }

    /**
     * Assert JSON path equals value.
     */
    public static void assertJsonPath(Response response, String jsonPath, Object expected) {
        Object actual = response.jsonPath().get(jsonPath);
        log.debug("Asserting JSON path {}: expected={}, actual={}", jsonPath, expected, actual);
        assertThat(actual)
                .as("JSON path: " + jsonPath)
                .isEqualTo(expected);
    }

    /**
     * Assert JSON path is not null or empty.
     */
    public static void assertJsonPathNotEmpty(Response response, String jsonPath) {
        Object value = response.jsonPath().get(jsonPath);
        assertThat(value)
                .as("JSON path should not be empty: " + jsonPath)
                .isNotNull();

        if (value instanceof String s) {
            assertThat(s).isNotBlank();
        } else if (value instanceof List<?> list) {
            assertThat(list).isNotEmpty();
        }
    }

    /**
     * Assert response header value.
     */
    public static void assertHeader(Response response, String name, String expectedValue) {
        String actual = response.getHeader(name);
        log.debug("Asserting header {}: expected={}, actual={}", name, expectedValue, actual);
        assertThat(actual)
                .as("Header: " + name)
                .isEqualTo(expectedValue);
    }

    /**
     * Assert response header contains value.
     */
    public static void assertHeaderContains(Response response, String name, String substring) {
        String actual = response.getHeader(name);
        assertThat(actual)
                .as("Header " + name + " should contain: " + substring)
                .contains(substring);
    }

    /**
     * Assert body contains text.
     */
    public static void assertBodyContains(Response response, String text) {
        String body = response.getBody().asString();
        assertThat(body)
                .as("Response body should contain: " + text)
                .contains(text);
    }

    /**
     * Assert response time is below threshold.
     */
    public static void assertResponseTime(Response response, long maxMillis) {
        long actual = response.getTimeIn(TimeUnit.MILLISECONDS);
        log.debug("Asserting response time: max={}, actual={}", maxMillis, actual);
        assertThat(actual)
                .as("Response time should be less than " + maxMillis + "ms")
                .isLessThan(maxMillis);
    }

    /**
     * Assert array at JSON path has expected size.
     */
    public static void assertArraySize(Response response, String jsonPath, int expectedSize) {
        List<?> array = response.jsonPath().getList(jsonPath);
        assertThat(array)
                .as("Array at " + jsonPath)
                .hasSize(expectedSize);
    }

    /**
     * Assert array at JSON path is not empty.
     */
    public static void assertArrayNotEmpty(Response response, String jsonPath) {
        List<?> array = response.jsonPath().getList(jsonPath);
        assertThat(array)
                .as("Array at " + jsonPath + " should not be empty")
                .isNotEmpty();
    }

    /**
     * Assert response matches JSON schema from classpath.
     */
    public static void assertJsonSchemaFromClasspath(Response response, String schemaPath) {
        log.debug("Validating against schema: {}", schemaPath);
        response.then().assertThat()
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(schemaPath));
    }

    /**
     * Assert response matches JSON schema from file.
     */
    public static void assertJsonSchema(Response response, File schemaFile) {
        log.debug("Validating against schema file: {}", schemaFile.getName());
        response.then().assertThat()
                .body(JsonSchemaValidator.matchesJsonSchema(schemaFile));
    }
}
