package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
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
                              @RequestBody ItemDto itemDto) {

        log.info("Получен запрос на создание предмета от пользователя id={}", userId);
        ItemDto createdItem = itemService.createItem(userId, itemDto);
        log.info("Предмет успешно создан: {}", createdItem);
        return createdItem;
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId) {

        log.info("Запрос предмета по id={}, userId={}", itemId, userId);

        ItemDto item = itemService.getItemById(userId, itemId);

        log.info("Предмет получен: id={}, userId={}", itemId, userId);

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

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody CommentDto commentDto) {

        log.info("Добавление комментария пользователем id={} к itemId={}", userId, itemId);
        CommentDto comment = itemService.addComment(userId, itemId, commentDto);
        log.info("Комментарий добавлен: {}", comment);
        return comment;
    }
}