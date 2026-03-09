package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDto {

    private Long id;

    @NotBlank(message = "Name must not be blank")
    private String name;

    private String description;

    @NotNull(message = "Available must not be null")
    private Boolean available;

    private Long ownerId;
}