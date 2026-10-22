package com.typeflow;

import com.typeflow.dto.RegisterRequest;
import com.typeflow.entity.User;
import com.typeflow.exception.ApiException;
import com.typeflow.repository.TypingResultRepository;
import com.typeflow.repository.UserRepository;
import com.typeflow.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private UserRepository userRepository;
    private TypingResultRepository typingResultRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        typingResultRepository = Mockito.mock(TypingResultRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, typingResultRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Verify user registration with secure BCrypt password hashing")
    void testSuccessfulRegistration() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@typeflow.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        RegisterRequest req = new RegisterRequest("newuser", "new@typeflow.com", "secret123", "secret123");
        User user = userService.registerUser(req);

        assertNotNull(user);
        assertEquals("newuser", user.getUsername());
        assertNotEquals("secret123", user.getPassword(), "Password must be securely hashed, never plaintext");
        assertTrue(passwordEncoder.matches("secret123", user.getPassword()), "Password hash must verify with BCrypt");
    }

    @Test
    @DisplayName("Verify rejection of password mismatch")
    void testPasswordMismatch() {
        RegisterRequest req = new RegisterRequest("user", "user@test.com", "pass123", "wrongpass");
        assertThrows(ApiException.class, () -> userService.registerUser(req));
    }

    @Test
    @DisplayName("Verify duplicate username rejection")
    void testDuplicateUsername() {
        when(userRepository.existsByUsername("taken")).thenReturn(true);
        RegisterRequest req = new RegisterRequest("taken", "unique@test.com", "pass123", "pass123");
        assertThrows(ApiException.class, () -> userService.registerUser(req));
    }

    @Test
    @DisplayName("Verify duplicate email rejection")
    void testDuplicateEmail() {
        when(userRepository.existsByUsername("unique")).thenReturn(false);
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);
        RegisterRequest req = new RegisterRequest("unique", "taken@test.com", "pass123", "pass123");
        assertThrows(ApiException.class, () -> userService.registerUser(req));
    }
}
