package com.splito.controller;

import com.splito.dto.mapper.UserMapper;
import com.splito.dto.request.CreateUserRequest;
import com.splito.dto.request.UpdateUserRequest;
import com.splito.dto.response.UserResponse;
import com.splito.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    // USE SIGNUP API FOR USER REGISTRATION
    @PostMapping
    public UserResponse create(@Valid @RequestBody CreateUserRequest req) {
        return userMapper.toResponse(userService.create(req));
    }

    // used for group member pickers etc (protected)
    @GetMapping
    public List<UserResponse> list() {
        return userService.list().stream().map(userMapper::toResponse).toList();
    }

    @GetMapping("/{userId}")
    public UserResponse get(@PathVariable Long userId) {
        return userMapper.toResponse(userService.get(userId));
    }

    // safe self endpoints
    @GetMapping("/me")
    public UserResponse me() {
        return userMapper.toResponse(userService.me());
    }

    @PutMapping("/me")
    public UserResponse updateMe(@Valid @RequestBody UpdateUserRequest req) {
        return userMapper.toResponse(userService.updateMe(req));
    }
}
