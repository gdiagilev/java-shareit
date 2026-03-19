package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @Valid @RequestBody ItemDto itemDto) {

        log.info("Получен запрос на создание предмета от пользователя id={}", userId);
        ItemDto createdItem = itemService.createItem(userId, itemDto);
        log.info("Предмет успешно создан: {}", createdItem);
        return createdItem;
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId) {

        log.info("Запрос предмета по id={}", itemId);
        ItemDto item = itemService.getItemById(itemId);
        log.info("Предмет получен: {}", item);
        return item;
    }

    @GetMapping
    public List<ItemDto> getUserItems(@RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info("Запрос всех предметов пользователя id={}", userId);
        List<ItemDto> items = itemService.getUserItems(userId);
        log.info("Найдено {} предметов пользователя id={}", items.size(), userId);
        return items;
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable Long itemId,
                              @RequestBody ItemDto itemDto) {

        log.info("Пользователь id={} пытается обновить предмет id={}", userId, itemId);
        ItemDto updatedItem = itemService.updateItem(userId, itemId, itemDto);
        log.info("Предмет id={} обновлён: {}", itemId, updatedItem);
        return updatedItem;
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {

        log.info("Поиск предметов по тексту '{}'", text);
        List<ItemDto> foundItems = itemService.searchItems(text);
        log.info("Найдено {} предметов по тексту '{}'", foundItems.size(), text);
        return foundItems;
    }
}