package config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static java.lang.invoke.MethodHandles.lookup;

/**
 * Centralized configuration management for API testing.
 *
 * <p>Supports environment-specific configurations with property resolution order:</p>
 * <ol>
 *   <li>System properties (-Dkey=value)</li>
 *   <li>Environment variables (KEY_NAME)</li>
 *   <li>Properties file (config/{env}.properties or config/test.properties)</li>
 * </ol>
 *
 * <h2>Usage:</h2>
 * <pre>
 * // Get singleton instance
 * ApiConfig config = ApiConfig.getInstance();
 *
 * // Access configuration
 * String baseUrl = config.getBaseUrl();
 * int timeout = config.getTimeout();
 *
 * // Switch environments
 * mvn test -Denv=staging
 * </pre>
 */
public class ApiConfig {

    private static final Logger log = LoggerFactory.getLogger(lookup().lookupClass());

    private static volatile ApiConfig instance;
    private final Properties properties;

    // Configuration keys
    public static final String BASE_URL = "api.base.url";
    public static final String TIMEOUT = "api.timeout";
    public static final String AUTH_USERNAME = "api.auth.username";
    public static final String AUTH_PASSWORD = "api.auth.password";
    public static final String LOG_REQUESTS = "api.log.requests";
    public static final String LOG_RESPONSES = "api.log.responses";

    private ApiConfig() {
        this.properties = new Properties();
        loadConfiguration();
        logConfiguration();
    }

    /**
     * Get singleton instance (thread-safe).
     */
    public static ApiConfig getInstance() {
        if (instance == null) {
            synchronized (ApiConfig.class) {
                if (instance == null) {
                    instance = new ApiConfig();
                }
            }
        }
        return instance;
    }

    /**
     * Load configuration from properties file based on environment.
     */
    private void loadConfiguration() {
        String env = System.getProperty("env", "test");
        String configFile = "config/" + env + ".properties";

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(configFile)) {
            if (input != null) {
                properties.load(input);
                log.info("Loaded configuration from: {}", configFile);
            } else {
                // Fallback to default test.properties
                try (InputStream defaultInput = getClass().getClassLoader()
                        .getResourceAsStream("config/test.properties")) {
                    if (defaultInput != null) {
                        properties.load(defaultInput);
                        log.info("Loaded default configuration: config/test.properties");
                    } else {
                        log.warn("No configuration file found, using defaults");
                    }
                }
            }
        } catch (IOException e) {
            log.error("Failed to load configuration", e);
            throw new RuntimeException("Configuration loading failed", e);
        }
    }

    /**
     * Log current configuration (masks sensitive data).
     */
    private void logConfiguration() {
        log.info("=== API Configuration ===");
        log.info("Base URL: {}", getBaseUrl());
        log.info("Timeout: {} seconds", getTimeout());
        log.info("Request Logging: {}", isRequestLoggingEnabled());
        log.info("Response Logging: {}", isResponseLoggingEnabled());
        log.info("=========================");
    }

    // ==================== Property Resolution ====================

    /**
     * Resolve property with priority: System > Environment > Properties file.
     */
    private String resolveProperty(String key) {
        // 1. System property
        String value = System.getProperty(key);
        if (value != null) return value;

        // 2. Environment variable (convert key.name to KEY_NAME)
        String envKey = key.toUpperCase().replace(".", "_");
        value = System.getenv(envKey);
        if (value != null) return value;

        // 3. Properties file
        return properties.getProperty(key);
    }

    private String resolveProperty(String key, String defaultValue) {
        String value = resolveProperty(key);
        return value != null ? value : defaultValue;
    }

    private int resolveIntProperty(String key, int defaultValue) {
        String value = resolveProperty(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid integer for {}: {}, using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    private boolean resolveBooleanProperty(String key, boolean defaultValue) {
        String value = resolveProperty(key);
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value.trim());
    }

    // ==================== Configuration Getters ====================

    /**
     * Get the base URL for the API under test.
     */
    public String getBaseUrl() {
        return resolveProperty(BASE_URL, "https://restful-booker.herokuapp.com");
    }

    /**
     * Get request timeout in seconds.
     */
    public int getTimeout() {
        return resolveIntProperty(TIMEOUT, 30);
    }

    /**
     * Get authentication username.
     */
    public String getAuthUsername() {
        return resolveProperty(AUTH_USERNAME, "admin");
    }

    /**
     * Get authentication password.
     */
    public String getAuthPassword() {
        return resolveProperty(AUTH_PASSWORD, "password123");
    }

    /**
     * Check if request logging is enabled.
     */
    public boolean isRequestLoggingEnabled() {
        return resolveBooleanProperty(LOG_REQUESTS, true);
    }

    /**
     * Check if response logging is enabled.
     */
    public boolean isResponseLoggingEnabled() {
        return resolveBooleanProperty(LOG_RESPONSES, true);
    }

    /**
     * Reset configuration (for testing purposes).
     */
    public static synchronized void reset() {
        instance = null;
    }
}
