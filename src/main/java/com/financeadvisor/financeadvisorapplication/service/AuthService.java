package com.financeadvisor.financeadvisorapplication.service;

import com.financeadvisor.financeadvisorapplication.dto.AuthResponse;
import com.financeadvisor.financeadvisorapplication.dto.LoginRequest;
import com.financeadvisor.financeadvisorapplication.dto.RegisterRequest;
import com.financeadvisor.financeadvisorapplication.dto.UserResponse;
import com.financeadvisor.financeadvisorapplication.entity.User;
import com.financeadvisor.financeadvisorapplication.repository.UserRepository;
import com.financeadvisor.financeadvisorapplication.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public UserDetails loadUserByUsername(String email) {
        // Find user by email
        // If not found throw UsernameNotFoundException
        // Return UserDetails object
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(new ArrayList<>())
                .build();
    }

    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        // Hash the password
        // Build and save user
        // Return saved user
        var existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(hashedPassword);

        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(savedUser.getId());
        return new AuthResponse(token, savedUser.getId());


    }

    public AuthResponse login(LoginRequest request) {
        // Find user by email
        // Compare passwords using passwordEncoder.matches()
        // If wrong password throw an exception
        // Generate JWT using jwtUtil
        // Return AuthResponse with token and userId
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Wrong password");
        }
        String token = jwtUtil.generateToken(user.getId());
        return new AuthResponse(token, user.getId());
    }


}