package com.financeadvisor.financeadvisorapplication.controller;

import com.financeadvisor.financeadvisorapplication.dto.AuthResponse;
import com.financeadvisor.financeadvisorapplication.dto.LoginRequest;
import com.financeadvisor.financeadvisorapplication.dto.RegisterRequest;
import com.financeadvisor.financeadvisorapplication.dto.UserResponse;
import com.financeadvisor.financeadvisorapplication.entity.User;
import com.financeadvisor.financeadvisorapplication.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
       AuthResponse response =  authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

}
