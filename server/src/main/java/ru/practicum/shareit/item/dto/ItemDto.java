package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotBlank(message = "Description must not be blank")
    private String description;

    @NotNull(message = "Available must not be null")
    private Boolean available;

    private Long ownerId;

    private BookingShortDto lastBooking;
    private BookingShortDto nextBooking;

    @Builder.Default
    private List<CommentDto> comments = new ArrayList<>();

    private Long requestId;
}