package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDto {

    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private Long ownerId;

    private BookingShortDto lastBooking;
    private BookingShortDto nextBooking;

    @Builder.Default
    private List<CommentDto> comments = new ArrayList<>();

    private Long requestId;
}