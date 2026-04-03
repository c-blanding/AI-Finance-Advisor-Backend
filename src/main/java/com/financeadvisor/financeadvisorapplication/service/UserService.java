package com.financeadvisor.financeadvisorapplication.service;

import com.financeadvisor.financeadvisorapplication.entity.User;
import com.financeadvisor.financeadvisorapplication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User getUserById(UUID userId)
    {
       return userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException("User not found")
        );
    }

    public String getUserCursor(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException("User not found")
        );
        return user.getPlaidCursor();
    }
}
