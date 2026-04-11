package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.item.dto.CommentMapper;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements ru.practicum.shareit.item.CommentService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long itemId, CommentDto commentDto) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        var item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        Comment comment = CommentMapper.toComment(commentDto, user, item);
        Comment saved = commentRepository.save(comment);

        return CommentMapper.toCommentDto(saved);
    }
}