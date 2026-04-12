package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository requestRepository;

    @Override
    @Transactional
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        Item item = ItemMapper.toItem(itemDto, owner);

        if (itemDto.getRequestId() != null) {
            ItemRequest request = requestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Request not found"));
            item.setRequest(request);
        }
        return ItemMapper.toDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("You are not the owner of this item");
        }

        if (itemDto.getName() != null) item.setName(itemDto.getName());
        if (itemDto.getDescription() != null) item.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() != null) item.setAvailable(itemDto.getAvailable());

        return ItemMapper.toDto(itemRepository.save(item));
    }

    @Override
    public ItemDto getItemById(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        ItemDto dto = ItemMapper.toDto(item);

        List<CommentDto> comments = commentRepository.findByItemIdOrderByCreatedDesc(itemId)
                .stream()
                .map(this::toDto)
                .toList();
        dto.setComments(comments);

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            Booking lastBooking = bookingRepository
                    .findFirstByItemIdAndStartBeforeOrderByStartDesc(itemId, now)
                    .orElse(null);
            Booking nextBooking = bookingRepository
                    .findFirstByItemIdAndStartAfterOrderByStartAsc(itemId, now)
                    .orElse(null);

            dto.setLastBooking(lastBooking != null ? mapToShort(lastBooking) : null);
            dto.setNextBooking(nextBooking != null ? mapToShort(nextBooking) : null);
        }

        return dto;
    }

    @Override
    public List<ItemDto> getUserItems(Long userId) {
        List<Item> items = itemRepository.findByOwnerId(userId);
        if (items.isEmpty()) return List.of();

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();

        LocalDateTime now = LocalDateTime.now();

        var comments = commentRepository.findByItemIdInOrderByCreatedDesc(itemIds);

        var commentsMap = comments.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getItem().getId()
                ));

        var lastBookings = bookingRepository
                .findByItemIdInAndStartBeforeOrderByStartDesc(itemIds, now);

        var lastMap = lastBookings.stream()
                .collect(Collectors.toMap(
                        b -> b.getItem().getId(),
                        b -> b,
                        (existing, replacement) -> existing
                ));

        var nextBookings = bookingRepository
                .findByItemIdInAndStartAfterOrderByStartAsc(itemIds, now);

        var nextMap = nextBookings.stream()
                .collect(Collectors.toMap(
                        b -> b.getItem().getId(),
                        b -> b,
                        (existing, replacement) -> existing
                ));

        return items.stream()
                .map(item -> {
                    ItemDto dto = ItemMapper.toDto(item);

                    dto.setLastBooking(mapToShort(lastMap.get(item.getId())));
                    dto.setNextBooking(mapToShort(nextMap.get(item.getId())));

                    List<CommentDto> itemComments = commentsMap
                            .getOrDefault(item.getId(), List.of())
                            .stream()
                            .map(this::toDto)
                            .toList();

                    dto.setComments(itemComments);

                    return dto;
                })
                .toList();
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) return List.of();
        return itemRepository.search(text)
                .stream()
                .map(ItemMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        boolean canComment = bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                userId, itemId, LocalDateTime.now(), BookingStatus.APPROVED
        );

        if (!canComment) {
            throw new IllegalArgumentException("User cannot comment without completed booking");
        }

        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(user)
                .created(LocalDateTime.now())
                .build();

        Comment saved = commentRepository.save(comment);

        return toDto(saved);
    }

    private BookingShortDto mapToShort(Booking booking) {
        if (booking == null) return null;
        return BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }

    private CommentDto toDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }
}