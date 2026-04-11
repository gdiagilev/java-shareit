package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;

import java.util.List;

public class ItemRequestMapper {

    public static ItemRequestDto toDto(ItemRequest request, List<Item> items) {

        ItemRequestDto dto = new ItemRequestDto();

        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());

        if (items == null || items.isEmpty()) {
            dto.setItems(List.of());
            return dto;
        }

        dto.setItems(
                items.stream()
                        .filter(item -> item != null)
                        .map(item -> new ItemShortDto(
                                item.getId(),
                                item.getName(),
                                item.getOwner() != null ? item.getOwner().getId() : null
                        ))
                        .toList()
        );

        return dto;
    }

    public static ItemRequest toEntity(ItemRequestDto dto) {
        return ItemRequest.builder()
                .description(dto.getDescription())
                .build();
    }
}