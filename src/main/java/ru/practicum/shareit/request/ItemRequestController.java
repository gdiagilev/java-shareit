package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService requestService;

    @PostMapping
    public ItemRequestDto createRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                        @RequestBody ItemRequestDto dto) {

        log.info("Пользователь id={} создает новый запрос предмета: {}", userId, dto);
        ItemRequestDto createdRequest = requestService.createRequest(userId, dto);
        log.info("Запрос предмета успешно создан: {}", createdRequest);
        return createdRequest;
    }

    @GetMapping
    public List<ItemRequestDto> getUserRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info("Запрос всех запросов пользователя id={}", userId);
        List<ItemRequestDto> requests = requestService.getUserRequests(userId);
        log.info("Найдено {} запросов пользователя id={}", requests.size(), userId);
        return requests;
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getOtherUsersRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info("Запрос всех запросов других пользователей от пользователя id={}", userId);
        List<ItemRequestDto> requests = requestService.getOtherUsersRequests(userId);
        log.info("Найдено {} запросов других пользователей", requests.size());
        return requests;
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @PathVariable Long requestId) {

        log.info("Пользователь id={} запрашивает запрос предмета id={}", userId, requestId);
        ItemRequestDto request = requestService.getRequestById(userId, requestId);
        log.info("Запрос предмета получен: {}", request);
        return request;
    }
}