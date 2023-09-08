package ru.practicum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.UserDto;
import ru.practicum.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/users")
@Slf4j
@Validated
public class AdminUserController {

    private final UserService userService;

    @Autowired
    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@Valid @RequestBody UserDto dto) {
        log.info("Received POST request to add user {}", dto.toString());
        return userService.addUser(dto);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserById(@PathVariable Long userId) {
        log.info("Received DELETE request to remove user by id {}", userId);
        userService.deleteUserById(userId);
    }

    @GetMapping()
    public List<UserDto> findAllUsers(@RequestParam(required = false) List<Long> ids,
                                      @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                      @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Received GET request to find all users");
        return userService.findAllUsers(ids, from, size);
    }
}
