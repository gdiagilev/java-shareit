package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.CreateUserDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody CreateUserDto dto) {
        log.info("Создание нового пользователя: {}", dto);
        return userService.createUser(dto);
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable Long id) {

        log.info("Запрос пользователя по id={}", id);
        UserDto user = userService.getUserById(id);
        log.info("Пользователь получен: {}", user);
        return user;
    }

    @GetMapping
    public List<UserDto> getAllUsers() {

        log.info("Запрос всех пользователей");
        List<UserDto> users = userService.getAllUsers();
        log.info("Найдено {} пользователей", users.size());
        return users;
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@PathVariable Long id,
                              @RequestBody UserDto updateUserDto) {

        log.info("Обновление пользователя id={} с данными: {}", id, updateUserDto);
        UserDto updatedUser = userService.updateUser(id, updateUserDto);
        log.info("Пользователь id={} обновлён: {}", id, updatedUser);
        return updatedUser;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {

        log.info("Удаление пользователя id={}", id);
        userService.deleteUser(id);
        log.info("Пользователь id={} удалён", id);
    }
}