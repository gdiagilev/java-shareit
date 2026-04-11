package ru.practicum.shareit.user.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDto;

@Component
public class UserClient extends BaseClient {

    private static final String API_PREFIX = "/users";
    private final String serverUrl;

    public UserClient(@Value("${shareit.server.url}") String serverUrl,
                      RestTemplate restTemplate) {
        super(restTemplate);
        this.serverUrl = serverUrl;
    }

    public ResponseEntity<Object> create(UserDto dto) {
        return post(serverUrl + API_PREFIX, null, dto);
    }

    public ResponseEntity<Object> update(Long userId, UserDto dto) {
        return patch(serverUrl + API_PREFIX + "/" + userId, null, dto);
    }

    public ResponseEntity<Object> get(Long userId) {
        return get(serverUrl + API_PREFIX + "/" + userId, null, null);
    }

    public ResponseEntity<Object> delete(Long userId) {
        return delete(serverUrl + API_PREFIX + "/" + userId, null);
    }

    public ResponseEntity<Object> getAll() {
        return get(serverUrl + API_PREFIX, null, null);
    }
}