package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService requestService;

    private ItemRequestDto makeRequestDto(Long id) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(id);
        dto.setDescription("Need a drill");
        dto.setCreated(LocalDateTime.now());
        dto.setItems(List.of());
        return dto;
    }

    @Test
    void createRequest_shouldReturn200WithBody() throws Exception {
        ItemRequestDto input = new ItemRequestDto();
        input.setDescription("Need a drill");

        when(requestService.createRequest(anyLong(), any())).thenReturn(makeRequestDto(1L));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need a drill"))
                .andExpect(jsonPath("$.created").exists())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getUserRequests_shouldReturn200WithList() throws Exception {
        when(requestService.getUserRequests(anyLong()))
                .thenReturn(List.of(makeRequestDto(1L), makeRequestDto(2L)));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getOtherUsersRequests_shouldReturn200WithList() throws Exception {
        when(requestService.getOtherUsersRequests(anyLong()))
                .thenReturn(List.of(makeRequestDto(2L)));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getRequestById_shouldReturn200WithItemsInResponse() throws Exception {
        ItemRequestDto dto = makeRequestDto(1L);
        dto.setItems(List.of(new ItemShortDto(10L, "Drill", 2L)));

        when(requestService.getRequestById(anyLong(), anyLong())).thenReturn(dto);

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].name").value("Drill"));
    }

    @Test
    void getRequestById_notFound_shouldReturn404() throws Exception {
        when(requestService.getRequestById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Request not found"));

        mockMvc.perform(get("/requests/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Request not found"));
    }
}
