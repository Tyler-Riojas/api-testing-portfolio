package api;

import config.ApiConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reporting.ExtentReportManager;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static java.lang.invoke.MethodHandles.lookup;

/**
 * REST API client with automatic request/response logging to Extent Reports.
 *
 * <p>Provides a fluent interface for HTTP operations with built-in:</p>
 * <ul>
 *   <li>Request/response logging to HTML reports</li>
 *   <li>Configurable base URL and timeouts</li>
 *   <li>Support for headers, query params, and auth</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <pre>
 * ApiClient client = new ApiClient();
 *
 * // Simple GET
 * Response response = client.get("/booking/1");
 *
 * // POST with body
 * Response response = client.post("/booking", bookingData);
 *
 * // With custom headers
 * Response response = client.withHeaders(Map.of("Cookie", "token=abc123"))
 *                           .put("/booking/1", updatedData);
 * </pre>
 */
public class ApiClient {

    private static final Logger log = LoggerFactory.getLogger(lookup().lookupClass());

    private final String baseUrl;
    private Map<String, String> headers;
    private Map<String, Object> queryParams;

    /**
     * Create client with default base URL from configuration.
     */
    public ApiClient() {
        this.baseUrl = ApiConfig.getInstance().getBaseUrl();
        log.debug("ApiClient initialized with base URL: {}", baseUrl);
    }

    /**
     * Create client with custom base URL.
     */
    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        log.debug("ApiClient initialized with custom URL: {}", baseUrl);
    }

    // ==================== Fluent Configuration ====================

    /**
     * Add custom headers to the next request.
     */
    public ApiClient withHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Add query parameters to the next request.
     */
    public ApiClient withQueryParams(Map<String, Object> queryParams) {
        this.queryParams = queryParams;
        return this;
    }

    // ==================== HTTP Methods ====================

    /**
     * Perform GET request.
     */
    public Response get(String endpoint) {
        Response response = buildRequest()
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();

        logToReport("GET", endpoint, null, response);
        resetState();
        return response;
    }

    /**
     * Perform POST request with JSON body.
     */
    public Response post(String endpoint, Object body) {
        Response response = buildRequest()
                .body(body)
                .when()
                .post(endpoint)
                .then()
                .extract()
                .response();

        logToReport("POST", endpoint, body, response);
        resetState();
        return response;
    }

    /**
     * Perform PUT request with JSON body.
     */
    public Response put(String endpoint, Object body) {
        Response response = buildRequest()
                .body(body)
                .when()
                .put(endpoint)
                .then()
                .extract()
                .response();

        logToReport("PUT", endpoint, body, response);
        resetState();
        return response;
    }

    /**
     * Perform PATCH request with JSON body.
     */
    public Response patch(String endpoint, Object body) {
        Response response = buildRequest()
                .body(body)
                .when()
                .patch(endpoint)
                .then()
                .extract()
                .response();

        logToReport("PATCH", endpoint, body, response);
        resetState();
        return response;
    }

    /**
     * Perform DELETE request.
     */
    public Response delete(String endpoint) {
        Response response = buildRequest()
                .when()
                .delete(endpoint)
                .then()
                .extract()
                .response();

        logToReport("DELETE", endpoint, null, response);
        resetState();
        return response;
    }

    // ==================== Request Building ====================

    /**
     * Build request specification with configured settings.
     * NOTE: Restful Booker API requires explicit header strings, not ContentType enum.
     */
    private RequestSpecification buildRequest() {
        RequestSpecification spec = given()
                .baseUri(baseUrl)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");

        if (headers != null) {
            spec.headers(headers);
        }

        if (queryParams != null) {
            spec.queryParams(queryParams);
        }

        return spec;
    }

    /**
     * Reset transient state after request.
     */
    private void resetState() {
        this.headers = null;
        this.queryParams = null;
    }

    // ==================== Reporting ====================

    /**
     * Log request/response details to Extent Report.
     */
    private void logToReport(String method, String endpoint, Object body, Response response) {
        if (ExtentReportManager.getTest() == null) {
            log.debug("No active test context, skipping report logging");
            return;
        }

        ApiReporter.logRequest(method, baseUrl + endpoint, headers, body, queryParams);
        ApiReporter.logResponse(response);
    }

    /**
     * Get the base URL for this client.
     */
    public String getBaseUrl() {
        return baseUrl;
    }
}
