package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.booking.dto.BookingShortDto;

class ItemWithBookingDto extends ItemDto {
    BookingShortDto lastBooking;
    BookingShortDto nextBooking;
}