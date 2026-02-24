package tests;

import api.ApiClient;
import api.AuthService;
import api.ResponseValidator;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive API tests for the Restful Booker booking endpoints.
 *
 * <p>Demonstrates:</p>
 * <ul>
 *   <li>Full CRUD operations (GET, POST, PUT, PATCH, DELETE)</li>
 *   <li>Request chaining with data dependencies</li>
 *   <li>Token-based authentication</li>
 *   <li>Fluent response validation</li>
 *   <li>Rich HTML reporting</li>
 * </ul>
 *
 * @see <a href="https://restful-booker.herokuapp.com/apidoc/">API Documentation</a>
 */
public class BookingApiTest {

    private static final Logger log = LoggerFactory.getLogger(lookup().lookupClass());

    private ApiClient client;
    private int bookingId;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        client = new ApiClient();
        log.info("Test suite initialized with base URL: {}", client.getBaseUrl());
    }

    // ==================== Health Check ====================

    @Test(description = "Verify API is available via health check endpoint",
          groups = {"smoke", "health"})
    public void testHealthCheck() {
        Response response = client.get("/ping");

        // Restful Booker returns 201 for ping
        ResponseValidator.assertStatusCode(response, 201);
    }

    // ==================== GET Operations ====================

    @Test(description = "Retrieve all booking IDs",
          groups = {"smoke", "crud", "get"})
    public void testGetAllBookings() {
        Response response = client.get("/booking");

        new ResponseValidator(response)
                .statusCode(200)
                .responseTimeLessThan(5000);

        // Verify response is a non-empty array
        assertThat(response.jsonPath().getList("$"))
                .as("Bookings list")
                .isNotEmpty();

        log.info("Retrieved {} bookings", response.jsonPath().getList("$").size());
    }

    @Test(description = "Retrieve bookings filtered by guest name",
          groups = {"crud", "get"})
    public void testGetBookingsWithFilter() {
        Map<String, Object> params = Map.of(
                "firstname", "John",
                "lastname", "Smith"
        );

        Response response = client
                .withQueryParams(params)
                .get("/booking");

        new ResponseValidator(response)
                .statusCode(200);

        log.info("Filtered search returned {} results", response.jsonPath().getList("$").size());
    }

    @Test(description = "Retrieve a specific booking by ID",
          groups = {"crud", "get"},
          dependsOnMethods = "testCreateBooking")
    public void testGetBookingById() {
        Response response = client.get("/booking/" + bookingId);

        new ResponseValidator(response)
                .statusCode(200)
                .hasJsonPath("firstname")
                .hasJsonPath("lastname")
                .hasJsonPath("totalprice")
                .hasJsonPath("depositpaid")
                .hasJsonPath("bookingdates.checkin")
                .hasJsonPath("bookingdates.checkout")
                .jsonPathEquals("firstname", "Jane")
                .jsonPathEquals("lastname", "Doe");

        log.info("Retrieved booking {} for {} {}",
                bookingId,
                response.jsonPath().getString("firstname"),
                response.jsonPath().getString("lastname"));
    }

    @Test(description = "Handle request for non-existent booking",
          groups = {"crud", "get", "negative"})
    public void testGetNonExistentBooking() {
        Response response = client.get("/booking/999999999");

        new ResponseValidator(response)
                .statusCode(404);
    }

    // ==================== POST Operations ====================

    @Test(description = "Create a new booking with all fields",
          groups = {"smoke", "crud", "post"},
          priority = 1)
    public void testCreateBooking() {
        Map<String, Object> booking = createBookingPayload(
                "Jane", "Doe", 250, true,
                "2024-06-01", "2024-06-07", "Late checkout"
        );

        Response response = client.post("/booking", booking);

        new ResponseValidator(response)
                .statusCode(200)
                .hasJsonPath("bookingid")
                .jsonPathEquals("booking.firstname", "Jane")
                .jsonPathEquals("booking.lastname", "Doe")
                .jsonPathEquals("booking.totalprice", 250)
                .jsonPathEquals("booking.depositpaid", true)
                .jsonPathEquals("booking.additionalneeds", "Late checkout");

        bookingId = response.jsonPath().getInt("bookingid");
        assertThat(bookingId).as("Booking ID").isPositive();

        log.info("Created booking with ID: {}", bookingId);
    }

    @Test(description = "Create booking with minimum required fields",
          groups = {"crud", "post", "validation"})
    public void testCreateBookingMinimalFields() {
        Map<String, Object> booking = new HashMap<>();
        booking.put("firstname", "Min");
        booking.put("lastname", "Fields");
        booking.put("totalprice", 100);
        booking.put("depositpaid", false);
        booking.put("bookingdates", Map.of(
                "checkin", "2024-07-01",
                "checkout", "2024-07-03"
        ));

        Response response = client.post("/booking", booking);

        new ResponseValidator(response)
                .statusCode(200)
                .hasJsonPath("bookingid");

        log.info("Minimal booking created successfully");
    }

    // ==================== PUT Operations ====================

    @Test(description = "Update entire booking with new data",
          groups = {"crud", "put"},
          dependsOnMethods = "testCreateBooking")
    public void testUpdateBooking() {
        Map<String, Object> updatedBooking = createBookingPayload(
                "Jane", "Updated", 350, true,
                "2024-06-10", "2024-06-15", "Airport transfer"
        );

        Response response = client
                .withHeaders(AuthService.getAuthHeaders())
                .put("/booking/" + bookingId, updatedBooking);

        new ResponseValidator(response)
                .statusCode(200)
                .jsonPathEquals("firstname", "Jane")
                .jsonPathEquals("lastname", "Updated")
                .jsonPathEquals("totalprice", 350)
                .jsonPathEquals("additionalneeds", "Airport transfer");

        log.info("Booking {} updated successfully", bookingId);
    }

    // ==================== PATCH Operations ====================

    @Test(description = "Partially update specific booking fields",
          groups = {"crud", "patch"},
          dependsOnMethods = "testUpdateBooking")
    public void testPartialUpdateBooking() {
        Map<String, Object> partialUpdate = Map.of(
                "firstname", "Janet",
                "totalprice", 400
        );

        Response response = client
                .withHeaders(AuthService.getAuthHeaders())
                .patch("/booking/" + bookingId, partialUpdate);

        new ResponseValidator(response)
                .statusCode(200)
                .jsonPathEquals("firstname", "Janet")
                .jsonPathEquals("totalprice", 400)
                // Verify unchanged fields remain
                .jsonPathEquals("lastname", "Updated");

        log.info("Booking {} partially updated", bookingId);
    }

    // ==================== DELETE Operations ====================

    @Test(description = "Delete booking and verify removal",
          groups = {"crud", "delete"},
          dependsOnMethods = "testPartialUpdateBooking",
          priority = 100)
    public void testDeleteBooking() {
        Response deleteResponse = client
                .withHeaders(AuthService.getAuthHeaders())
                .delete("/booking/" + bookingId);

        // Restful Booker returns 201 for successful delete
        ResponseValidator.assertStatusCode(deleteResponse, 201);

        // Verify booking no longer exists
        Response getResponse = client.get("/booking/" + bookingId);
        ResponseValidator.assertStatusCode(getResponse, 404);

        log.info("Booking {} deleted and verified", bookingId);
    }

    // ==================== Helper Methods ====================

    /**
     * Create a complete booking payload.
     */
    private Map<String, Object> createBookingPayload(
            String firstname, String lastname, int totalprice,
            boolean depositpaid, String checkin, String checkout,
            String additionalneeds) {

        Map<String, Object> booking = new HashMap<>();
        booking.put("firstname", firstname);
        booking.put("lastname", lastname);
        booking.put("totalprice", totalprice);
        booking.put("depositpaid", depositpaid);
        booking.put("additionalneeds", additionalneeds);
        booking.put("bookingdates", Map.of(
                "checkin", checkin,
                "checkout", checkout
        ));

        return booking;
    }
}
