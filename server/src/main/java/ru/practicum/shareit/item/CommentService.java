package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;

public interface CommentService {
    CommentDto createComment(Long userId, Long itemId, CommentDto commentDto);
}