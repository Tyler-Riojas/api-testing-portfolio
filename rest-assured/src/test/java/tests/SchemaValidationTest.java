package tests;

import api.ApiClient;
import api.ResponseValidator;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static java.lang.invoke.MethodHandles.lookup;

/**
 * JSON Schema validation tests for API responses.
 *
 * <p>Demonstrates:</p>
 * <ul>
 *   <li>Validating response structure against JSON schemas</li>
 *   <li>Ensuring API contract compliance</li>
 *   <li>Detecting breaking changes in API responses</li>
 * </ul>
 *
 * <p>Schemas are stored in {@code src/test/resources/schemas/}</p>
 */
public class SchemaValidationTest {

    private static final Logger log = LoggerFactory.getLogger(lookup().lookupClass());

    private ApiClient client;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        client = new ApiClient();
    }

    @Test(description = "Validate booking list response matches schema",
          groups = {"schema", "smoke"})
    public void testBookingListSchema() {
        Response response = client.get("/booking");

        new ResponseValidator(response)
                .statusCode(200)
                .matchesSchema("schemas/booking-list.json");

        log.info("Booking list schema validated successfully");
    }

    @Test(description = "Validate single booking response matches schema",
          groups = {"schema"})
    public void testBookingDetailSchema() {
        // First get a booking ID
        Response listResponse = client.get("/booking");
        int bookingId = listResponse.jsonPath().getInt("[0].bookingid");

        // Then validate the detail response
        Response response = client.get("/booking/" + bookingId);

        new ResponseValidator(response)
                .statusCode(200)
                .matchesSchema("schemas/booking-detail.json");

        log.info("Booking detail schema validated for ID: {}", bookingId);
    }

    @Test(description = "Validate booking creation response matches schema",
          groups = {"schema"})
    public void testBookingCreationSchema() {
        Map<String, Object> booking = Map.of(
                "firstname", "Schema",
                "lastname", "Test",
                "totalprice", 100,
                "depositpaid", true,
                "bookingdates", Map.of(
                        "checkin", "2024-08-01",
                        "checkout", "2024-08-05"
                )
        );

        Response response = client.post("/booking", booking);

        new ResponseValidator(response)
                .statusCode(200)
                .matchesSchema("schemas/booking-created.json");

        log.info("Booking creation schema validated");
    }

    @Test(description = "Validate auth token response matches schema",
          groups = {"schema", "auth"})
    public void testAuthTokenSchema() {
        Map<String, String> credentials = Map.of(
                "username", "admin",
                "password", "password123"
        );

        Response response = client.post("/auth", credentials);

        new ResponseValidator(response)
                .statusCode(200)
                .matchesSchema("schemas/auth-token.json");

        log.info("Auth token schema validated");
    }
}
