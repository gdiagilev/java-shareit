package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDto {

    private Long id;

    @NotNull(message = "itemId must not be null")
    private Long itemId;

    @NotNull(message = "start must not be null")
    @Future(message = "start must be in the future")
    private LocalDateTime start;

    @NotNull(message = "end must not be null")
    @Future(message = "end must be in the future")
    private LocalDateTime end;
}