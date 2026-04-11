package ru.practicum.shareit.request.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequestDto {

    private Long id;

    private String description;
}