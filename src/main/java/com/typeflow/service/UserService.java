package com.typeflow.service;

import com.typeflow.dto.RegisterRequest;
import com.typeflow.dto.UserProfileDto;
import com.typeflow.entity.TypingResult;
import com.typeflow.entity.User;
import com.typeflow.exception.ApiException;
import com.typeflow.repository.TypingResultRepository;
import com.typeflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TypingResultRepository typingResultRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       TypingResultRepository typingResultRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.typingResultRepository = typingResultRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ApiException("Passwords do not match");
        }

        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new ApiException("Username '" + username + "' is already taken");
        }

        if (userRepository.existsByEmail(email)) {
            throw new ApiException("Email '" + email + "' is already registered");
        }

        String initial = username.substring(0, 1).toUpperCase();
        User user = new User(username, email, passwordEncoder.encode(request.getPassword()), initial);
        return userRepository.save(user);
    }

    public Optional<User> findByUsernameOrEmail(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }
        String clean = identifier.trim();
        return userRepository.findByUsername(clean)
                .or(() -> userRepository.findByEmail(clean.toLowerCase()));
    }

    public UserProfileDto getUserProfile(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setAvatar(user.getAvatar());
        dto.setCreatedAt(user.getCreatedAt());

        List<TypingResult> results = typingResultRepository.findByUserIdOrderByCompletionTimestampDesc(user.getId());
        dto.setTotalTestsCompleted(results.size());

        if (!results.isEmpty()) {
            double pb = results.stream().mapToDouble(TypingResult::getNetWpm).max().orElse(0.0);
            double avg = results.stream().mapToDouble(TypingResult::getNetWpm).average().orElse(0.0);
            double bestAcc = results.stream().mapToDouble(TypingResult::getAccuracy).max().orElse(0.0);

            dto.setPersonalBestNetWpm(Math.round(pb * 10.0) / 10.0);
            dto.setAverageNetWpm(Math.round(avg * 10.0) / 10.0);
            dto.setBestAccuracy(Math.round(bestAcc * 100.0) / 100.0);
        } else {
            dto.setPersonalBestNetWpm(0.0);
            dto.setAverageNetWpm(0.0);
            dto.setBestAccuracy(0.0);
        }

        return dto;
    }

    @Transactional
    public void claimGuestResult(Long resultId, User user) {
        if (resultId == null || user == null) return;
        typingResultRepository.findById(resultId).ifPresent(result -> {
            if (result.getUser() == null) {
                result.setUser(user);
                typingResultRepository.save(result);
            }
        });
    }

    @Transactional
    public void deleteAccount(User user) {
        userRepository.delete(user);
    }
}
