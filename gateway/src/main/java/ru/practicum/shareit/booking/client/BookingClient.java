package ru.practicum.shareit.booking.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;

@Component
public class BookingClient extends BaseClient {

    private static final String API_PREFIX = "/bookings";
    private final String serverUrl;

    public BookingClient(RestTemplate restTemplate,
                         @Value("${shareit.server.url}") String serverUrl) {
        super(restTemplate);
        this.serverUrl = serverUrl;
    }

    public ResponseEntity<Object> createBooking(Object dto, Long userId) {
        return post(serverUrl + API_PREFIX, userId, dto);
    }

    public ResponseEntity<Object> approveBooking(Long bookingId, boolean approved, Long userId) {
        return patch(serverUrl + API_PREFIX + "/" + bookingId + "?approved=" + approved, userId, null);
    }

    public ResponseEntity<Object> getBooking(Long bookingId, Long userId) {
        return get(serverUrl + API_PREFIX + "/" + bookingId, userId, null);
    }

    public ResponseEntity<Object> getUserBookings(Long userId, String state) {
        return get(serverUrl + API_PREFIX + "?state=" + state, userId, null);
    }

    public ResponseEntity<Object> getOwnerBookings(Long userId, String state) {
        return get(serverUrl + API_PREFIX + "/owner?state=" + state, userId, null);
    }
}