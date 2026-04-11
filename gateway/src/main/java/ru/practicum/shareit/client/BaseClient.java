package ru.practicum.shareit.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class BaseClient {

    protected final RestTemplate rest;

    public BaseClient(RestTemplate rest) {
        this.rest = rest;
    }

    protected ResponseEntity<Object> get(String url, Long userId, Object body) {
        return makeAndSendRequest(HttpMethod.GET, url, userId, body);
    }

    protected ResponseEntity<Object> post(String url, Long userId, Object body) {
        return makeAndSendRequest(HttpMethod.POST, url, userId, body);
    }

    protected ResponseEntity<Object> patch(String url, Long userId, Object body) {
        return makeAndSendRequest(HttpMethod.PATCH, url, userId, body);
    }

    protected ResponseEntity<Object> delete(String url, Long userId) {
        return makeAndSendRequest(HttpMethod.DELETE, url, userId, null);
    }

    private ResponseEntity<Object> makeAndSendRequest(HttpMethod method, String url, Long userId, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (userId != null) {
            headers.add("X-Sharer-User-Id", userId.toString());
        }
        HttpEntity<Object> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Object> serverResponse = rest.exchange(url, method, request, Object.class);

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_JSON);

            return new ResponseEntity<>(
                    serverResponse.getBody(),
                    responseHeaders,
                    serverResponse.getStatusCode()
            );
        } catch (HttpStatusCodeException ex) {
            log.warn("Ошибка запроса: {} {}", method, url);

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_JSON);

            return new ResponseEntity<>(
                    ex.getResponseBodyAsString(),
                    responseHeaders,
                    ex.getStatusCode()
            );
        }
    }
}