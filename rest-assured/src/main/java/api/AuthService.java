package api;

import config.ApiConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static java.lang.invoke.MethodHandles.lookup;

/**
 * Authentication service for obtaining and managing API tokens.
 *
 * <p>Handles token-based authentication with automatic caching to avoid
 * redundant authentication calls during test execution.</p>
 *
 * <h2>Usage:</h2>
 * <pre>
 * // Get token (authenticates if needed)
 * String token = AuthService.getToken();
 *
 * // Use token in requests
 * client.withHeaders(Map.of("Cookie", "token=" + token))
 *       .put("/booking/1", data);
 *
 * // Force re-authentication
 * AuthService.invalidateToken();
 * String newToken = AuthService.getToken();
 * </pre>
 */
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(lookup().lookupClass());

    private static String cachedToken;
    private static long tokenTimestamp;
    private static final long TOKEN_TTL_MS = 30 * 60 * 1000; // 30 minutes

    private AuthService() {
        // Utility class
    }

    /**
     * Get authentication token, using cache if valid.
     */
    public static synchronized String getToken() {
        if (isTokenValid()) {
            log.debug("Using cached auth token");
            return cachedToken;
        }

        log.info("Authenticating to obtain new token...");
        cachedToken = authenticate();
        tokenTimestamp = System.currentTimeMillis();

        return cachedToken;
    }

    /**
     * Invalidate cached token, forcing re-authentication on next request.
     */
    public static synchronized void invalidateToken() {
        log.info("Invalidating cached auth token");
        cachedToken = null;
        tokenTimestamp = 0;
    }

    /**
     * Check if cached token is still valid.
     */
    private static boolean isTokenValid() {
        if (cachedToken == null) return false;
        long age = System.currentTimeMillis() - tokenTimestamp;
        return age < TOKEN_TTL_MS;
    }

    /**
     * Perform authentication and return token.
     */
    private static String authenticate() {
        ApiConfig config = ApiConfig.getInstance();
        String baseUrl = config.getBaseUrl();
        String username = config.getAuthUsername();
        String password = config.getAuthPassword();

        Map<String, String> credentials = Map.of(
                "username", username,
                "password", password
        );

        Response response = given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                .post("/auth")
                .then()
                .extract()
                .response();

        if (response.getStatusCode() != 200) {
            String error = "Authentication failed with status: " + response.getStatusCode();
            log.error(error);
            throw new AuthenticationException(error);
        }

        String token = response.jsonPath().getString("token");
        if (token == null || token.isEmpty()) {
            throw new AuthenticationException("No token received from auth endpoint");
        }

        log.info("Authentication successful, token obtained");
        return token;
    }

    /**
     * Get authorization header map with token cookie.
     */
    public static Map<String, String> getAuthHeaders() {
        return Map.of("Cookie", "token=" + getToken());
    }

    /**
     * Exception thrown when authentication fails.
     */
    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
        }
    }
}
