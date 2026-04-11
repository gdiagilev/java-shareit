package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto createRequest(Long userId, ItemRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ItemRequest request = ItemRequestMapper.toEntity(dto);
        request.setRequester(user);
        request.setCreated(LocalDateTime.now());

        return ItemRequestMapper.toDto(requestRepository.save(request), List.of());
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {

        List<ItemRequest> requests = requestRepository.findByRequester(userId);

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        Map<Long, List<Item>> itemsMap = itemRepository
                .findByRequestIdIn(requestIds)
                .stream()
                .filter(item -> item.getRequest() != null)
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return requests.stream()
                .map(r -> {
                    List<Item> items = itemsMap.getOrDefault(r.getId(), List.of());
                    return ItemRequestMapper.toDto(r, items);
                })
                .toList();
    }

    @Override
    public List<ItemRequestDto> getOtherUsersRequests(Long userId) {

        List<ItemRequest> requests = requestRepository.findAllExceptRequester(userId);

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        Map<Long, List<Item>> itemsMap = itemRepository
                .findByRequestIdIn(requestIds)
                .stream()
                .filter(item -> item.getRequest() != null)
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return requests.stream()
                .map(r -> {
                    List<Item> items = itemsMap.getOrDefault(r.getId(), List.of());
                    return ItemRequestMapper.toDto(r, items);
                })
                .toList();
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        List<Item> items = itemRepository.findByRequestIdIn(List.of(requestId));

        return ItemRequestMapper.toDto(request, items);
    }
}