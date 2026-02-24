package api;

import com.aventstack.extentreports.ExtentTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.restassured.http.Header;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reporting.ExtentReportManager;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.invoke.MethodHandles.lookup;

/**
 * Rich API request/response reporter for Extent Reports.
 *
 * <p>Generates Postman-style HTML visualizations of API calls including:</p>
 * <ul>
 *   <li>Color-coded HTTP method badges</li>
 *   <li>Request URL, headers, and body</li>
 *   <li>Response status, timing, headers, and body</li>
 *   <li>Pretty-printed JSON with syntax highlighting</li>
 * </ul>
 *
 * <p>This class is used internally by {@link ApiClient} and should not
 * typically be called directly.</p>
 */
public class ApiReporter {

    private static final Logger log = LoggerFactory.getLogger(lookup().lookupClass());
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private ApiReporter() {
        // Utility class
    }

    /**
     * Log request details to Extent Report.
     */
    public static void logRequest(String method, String url, Map<String, String> headers,
                                  Object body, Map<String, ?> queryParams) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test == null) return;

        StringBuilder html = new StringBuilder();

        // Request header with method badge
        html.append("<div style='background: #1e3a5f; color: white; padding: 12px; ")
            .append("border-radius: 6px 6px 0 0; font-weight: bold; font-family: -apple-system, BlinkMacSystemFont, sans-serif;'>");
        html.append("<span style='background: ").append(getMethodColor(method))
            .append("; padding: 4px 10px; border-radius: 4px; margin-right: 12px; font-size: 12px;'>")
            .append(method).append("</span>");
        html.append("<span style='font-family: monospace; font-size: 13px;'>").append(escapeHtml(url)).append("</span>");
        html.append("</div>");

        // Request body section
        html.append("<div style='background: #f8f9fa; border: 1px solid #dee2e6; border-top: none; ")
            .append("padding: 16px; border-radius: 0 0 6px 6px;'>");

        // Query Parameters
        if (queryParams != null && !queryParams.isEmpty()) {
            html.append("<div style='margin-bottom: 16px;'>");
            html.append("<div style='color: #495057; font-weight: 600; margin-bottom: 8px; font-size: 13px;'>Query Parameters</div>");
            html.append("<div style='background: #fff; border: 1px solid #e9ecef; border-radius: 4px; ")
                .append("padding: 12px; font-family: monospace; font-size: 12px;'>");
            queryParams.forEach((key, value) ->
                html.append("<div style='padding: 2px 0;'>")
                    .append("<span style='color: #6f42c1;'>").append(escapeHtml(key)).append("</span>")
                    .append("<span style='color: #6c757d;'> = </span>")
                    .append("<span style='color: #28a745;'>").append(escapeHtml(String.valueOf(value))).append("</span>")
                    .append("</div>")
            );
            html.append("</div></div>");
        }

        // Request Headers
        html.append("<div style='margin-bottom: 16px;'>");
        html.append("<div style='color: #495057; font-weight: 600; margin-bottom: 8px; font-size: 13px;'>Request Headers</div>");
        html.append("<div style='background: #fff; border: 1px solid #e9ecef; border-radius: 4px; ")
            .append("padding: 12px; font-family: monospace; font-size: 12px;'>");
        html.append("<div style='padding: 2px 0;'>Content-Type: application/json</div>");
        html.append("<div style='padding: 2px 0;'>Accept: application/json</div>");
        if (headers != null) {
            headers.forEach((key, value) -> {
                String displayValue = isSensitive(key) ? maskValue(value) : value;
                html.append("<div style='padding: 2px 0;'>")
                    .append(escapeHtml(key)).append(": ").append(escapeHtml(displayValue))
                    .append("</div>");
            });
        }
        html.append("</div></div>");

        // Request Body
        if (body != null) {
            html.append("<div>");
            html.append("<div style='color: #495057; font-weight: 600; margin-bottom: 8px; font-size: 13px;'>Request Body</div>");
            html.append("<pre style='background: #1e1e1e; color: #ffffff !important; padding: 16px; ")
                .append("border-radius: 4px; margin: 0; overflow-x: auto; font-size: 12px; line-height: 1.5;'>");
            html.append(escapeHtml(prettyPrintJson(body)));
            html.append("</pre></div>");
        }

        html.append("</div>");

        test.info(html.toString());
        log.info("{} {}", method, url);
    }

    /**
     * Log response details to Extent Report.
     */
    public static void logResponse(Response response) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test == null) return;

        int statusCode = response.getStatusCode();
        long responseTime = response.getTimeIn(TimeUnit.MILLISECONDS);
        String responseBody = response.getBody().asString();

        StringBuilder html = new StringBuilder();

        // Response header with status
        String statusColor = getStatusColor(statusCode);
        html.append("<div style='background: ").append(statusColor)
            .append("; color: white; padding: 12px; border-radius: 6px 6px 0 0; ")
            .append("font-weight: bold; margin-top: 16px; font-family: -apple-system, BlinkMacSystemFont, sans-serif;'>");
        html.append("<span style='font-size: 18px; margin-right: 8px;'>").append(statusCode).append("</span>");
        html.append("<span style='font-weight: normal; opacity: 0.9;'>").append(getStatusText(statusCode)).append("</span>");
        html.append("<span style='float: right; background: rgba(255,255,255,0.2); padding: 4px 12px; ")
            .append("border-radius: 4px; font-size: 13px;'>").append(responseTime).append(" ms</span>");
        html.append("</div>");

        // Response body section
        html.append("<div style='background: #f8f9fa; border: 1px solid #dee2e6; border-top: none; ")
            .append("padding: 16px; border-radius: 0 0 6px 6px;'>");

        // Response Headers
        html.append("<div style='margin-bottom: 16px;'>");
        html.append("<div style='color: #495057; font-weight: 600; margin-bottom: 8px; font-size: 13px;'>Response Headers</div>");
        html.append("<div style='background: #fff; border: 1px solid #e9ecef; border-radius: 4px; ")
            .append("padding: 12px; font-family: monospace; font-size: 11px; max-height: 120px; overflow-y: auto;'>");
        for (Header header : response.getHeaders()) {
            html.append("<div style='padding: 1px 0;'>")
                .append(escapeHtml(header.getName())).append(": ").append(escapeHtml(header.getValue()))
                .append("</div>");
        }
        html.append("</div></div>");

        // Response Body
        html.append("<div>");
        html.append("<div style='color: #495057; font-weight: 600; margin-bottom: 8px; font-size: 13px;'>Response Body</div>");
        if (responseBody != null && !responseBody.isEmpty()) {
            html.append("<pre style='background: #1e1e1e; color: #ffffff !important; padding: 16px; ")
                .append("border-radius: 4px; margin: 0; overflow-x: auto; font-size: 12px; ")
                .append("line-height: 1.5; max-height: 400px; overflow-y: auto;'>");
            html.append(escapeHtml(prettyPrintJsonString(responseBody)));
            html.append("</pre>");
        } else {
            html.append("<div style='color: #6c757d; font-style: italic;'>No response body</div>");
        }
        html.append("</div>");

        html.append("</div>");

        // Log at appropriate level based on status
        if (statusCode >= 200 && statusCode < 300) {
            test.info(html.toString());
        } else if (statusCode >= 400) {
            test.warning(html.toString());
        } else {
            test.info(html.toString());
        }

        log.info("Response: {} {} ({}ms)", statusCode, getStatusText(statusCode), responseTime);
    }

    // ==================== Helper Methods ====================

    private static String getMethodColor(String method) {
        return switch (method.toUpperCase()) {
            case "GET" -> "#61affe";
            case "POST" -> "#49cc90";
            case "PUT" -> "#fca130";
            case "PATCH" -> "#50e3c2";
            case "DELETE" -> "#f93e3e";
            default -> "#6c757d";
        };
    }

    private static String getStatusColor(int statusCode) {
        if (statusCode >= 200 && statusCode < 300) return "#28a745";
        if (statusCode >= 300 && statusCode < 400) return "#17a2b8";
        if (statusCode >= 400 && statusCode < 500) return "#ffc107";
        if (statusCode >= 500) return "#dc3545";
        return "#6c757d";
    }

    private static String getStatusText(int statusCode) {
        return switch (statusCode) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 418 -> "I'm a Teapot";
            case 500 -> "Internal Server Error";
            default -> "Status " + statusCode;
        };
    }

    private static String prettyPrintJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }

    private static String prettyPrintJsonString(String json) {
        try {
            Object obj = objectMapper.readValue(json, Object.class);
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return json;
        }
    }

    private static boolean isSensitive(String key) {
        String lower = key.toLowerCase();
        return lower.contains("auth") || lower.contains("token") ||
               lower.contains("password") || lower.contains("secret") ||
               lower.contains("cookie");
    }

    private static String maskValue(String value) {
        if (value == null || value.length() <= 8) return "********";
        return value.substring(0, 4) + "..." + value.substring(value.length() - 4);
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}
