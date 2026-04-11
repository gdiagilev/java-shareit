package ru.practicum.shareit.item.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;

@Component
public class ItemClient extends BaseClient {

    private static final String API_PREFIX = "/items";
    private final String serverUrl;

    public ItemClient(@Value("${shareit.server.url}") String serverUrl,
                      RestTemplate restTemplate) {
        super(restTemplate);
        this.serverUrl = serverUrl;
    }

    public ResponseEntity<Object> createItem(Object itemDto, Long userId) {
        return post(serverUrl + API_PREFIX, userId, itemDto);
    }

    public ResponseEntity<Object> getItem(Long itemId, Long userId) {
        return get(serverUrl + API_PREFIX + "/" + itemId, userId, null);
    }

    public ResponseEntity<Object> updateItem(Long itemId, Object itemDto, Long userId) {
        return patch(serverUrl + API_PREFIX + "/" + itemId, userId, itemDto);
    }

    public ResponseEntity<Object> getUserItems(Long userId) {
        return get(serverUrl + API_PREFIX, userId, null);
    }

    public ResponseEntity<Object> searchItems(String text) {
        return get(serverUrl + API_PREFIX + "/search?text=" + text, null, null);
    }

    public ResponseEntity<Object> addComment(Long itemId, Object commentDto, Long userId) {
        return post(serverUrl + API_PREFIX + "/" + itemId + "/comment", userId, commentDto);
    }
}