package com.financeadvisor.financeadvisorapplication.controller;

import com.financeadvisor.financeadvisorapplication.dto.UserResponse;
import com.financeadvisor.financeadvisorapplication.entity.User;
import com.financeadvisor.financeadvisorapplication.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    ResponseEntity<UserResponse> getUser(@RequestAttribute("userId") UUID userId) throws IOException {
       User user = userService.getUserById(userId);

        return ResponseEntity.ok(new UserResponse(user.getId(), user.getName(), user.getEmail()));

    }


}
