package ru.practicum.shareit.request.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;

@Component
public class ItemRequestClient extends BaseClient {

    private static final String API_PREFIX = "/requests";
    private final String serverUrl;

    public ItemRequestClient(@Value("${shareit.server.url}") String serverUrl,
                             RestTemplate restTemplate) {
        super(restTemplate);
        this.serverUrl = serverUrl;
    }

    public ResponseEntity<Object> createRequest(Long userId, Object requestDto) {
        return post(serverUrl + API_PREFIX, userId, requestDto);
    }

    public ResponseEntity<Object> getRequest(Long userId, Long requestId) {
        return get(serverUrl + API_PREFIX + "/" + requestId, userId, null);
    }

    public ResponseEntity<Object> getAllUserRequests(Long userId) {
        return get(serverUrl + API_PREFIX, userId, null);
    }

    public ResponseEntity<Object> getAllRequests(Long userId) {
        return get(serverUrl + API_PREFIX, userId, null);
    }
}