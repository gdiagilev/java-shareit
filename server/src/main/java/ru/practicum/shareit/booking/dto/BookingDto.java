package ru.practicum.shareit.booking.dto;

import lombok.*;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDto {

    private Long id;

    private Long itemId;

    private LocalDateTime start;

    private LocalDateTime end;

    private BookingStatus status;

    private UserShortDto booker;
    private ItemShortDto item;
}