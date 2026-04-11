package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private ItemDto makeItemDto(Long id) {
        return ItemDto.builder()
                .id(id)
                .name("Drill")
                .description("A powerful drill")
                .available(true)
                .build();
    }

    @Test
    void createItem_shouldReturn200WithBody() throws Exception {
        ItemDto input = ItemDto.builder()
                .name("Drill")
                .description("A powerful drill")
                .available(true)
                .build();

        when(itemService.createItem(anyLong(), any())).thenReturn(makeItemDto(1L));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Drill"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void createItem_withMissingName_shouldReturn400() throws Exception {
        ItemDto input = ItemDto.builder()
                .description("A powerful drill")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void createItem_withMissingAvailable_shouldReturn400() throws Exception {
        ItemDto input = ItemDto.builder()
                .name("Drill")
                .description("A powerful drill")
                .build();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItemById_shouldReturn200WithBody() throws Exception {
        when(itemService.getItemById(anyLong(), anyLong())).thenReturn(makeItemDto(1L));

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void getItemById_notFound_shouldReturn404() throws Exception {
        when(itemService.getItemById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Item not found"));

        mockMvc.perform(get("/items/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Item not found"));
    }

    @Test
    void getUserItems_shouldReturn200WithList() throws Exception {
        when(itemService.getUserItems(anyLong()))
                .thenReturn(List.of(makeItemDto(1L), makeItemDto(2L)));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void updateItem_shouldReturn200WithUpdatedBody() throws Exception {
        ItemDto update = ItemDto.builder().name("Updated Drill").build();
        ItemDto response = ItemDto.builder()
                .id(1L).name("Updated Drill").description("A powerful drill").available(true).build();

        when(itemService.updateItem(anyLong(), anyLong(), any())).thenReturn(response);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Drill"));
    }

    @Test
    void updateItem_byNonOwner_shouldReturn404() throws Exception {
        ItemDto update = ItemDto.builder().name("Hacked").build();

        when(itemService.updateItem(anyLong(), anyLong(), any()))
                .thenThrow(new NotFoundException("You are not the owner of this item"));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchItems_shouldReturn200WithMatchingItems() throws Exception {
        when(itemService.searchItems("drill")).thenReturn(List.of(makeItemDto(1L)));

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void searchItems_withBlankText_shouldReturnEmptyList() throws Exception {
        when(itemService.searchItems("")).thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void addComment_shouldReturn200WithComment() throws Exception {
        CommentDto input = CommentDto.builder().text("Great drill!").build();
        CommentDto response = CommentDto.builder()
                .id(1L)
                .text("Great drill!")
                .authorName("Booker")
                .created(LocalDateTime.now())
                .build();

        when(itemService.addComment(anyLong(), anyLong(), any())).thenReturn(response);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Great drill!"))
                .andExpect(jsonPath("$.authorName").value("Booker"))
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    void addComment_withBlankText_shouldReturn400() throws Exception {
        CommentDto input = CommentDto.builder().text("").build();

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }
}
