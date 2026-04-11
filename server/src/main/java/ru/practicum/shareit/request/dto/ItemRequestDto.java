package ru.practicum.shareit.request.dto;

import lombok.*;
import ru.practicum.shareit.item.dto.ItemShortDto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequestDto {

    private Long id;
    @NotBlank(message = "Description must not be blank")
    private String description;
    private LocalDateTime created;

    @Builder.Default
    private List<ItemShortDto> items = List.of();
}