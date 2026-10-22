package com.typeflow.controller;

import com.typeflow.dto.ApiResponse;
import com.typeflow.dto.PersonalStatsDto;
import com.typeflow.entity.User;
import com.typeflow.exception.ApiException;
import com.typeflow.service.StatsService;
import com.typeflow.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;
    private final UserService userService;

    @Autowired
    public StatsController(StatsService statsService, UserService userService) {
        this.statsService = statsService;
        this.userService = userService;
    }

    private User getAuthenticatedUser(Principal principal) {
        if (principal == null) {
            throw new ApiException("Authentication required");
        }
        return userService.findByUsernameOrEmail(principal.getName())
                .orElseThrow(() -> new ApiException("User not found"));
    }

    @GetMapping("/personal")
    public ResponseEntity<ApiResponse<PersonalStatsDto>> getPersonalStats(Principal principal) {
        User user = getAuthenticatedUser(principal);
        PersonalStatsDto stats = statsService.getPersonalStats(user);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @DeleteMapping("/result/{id}")
    public ResponseEntity<ApiResponse<String>> deleteResult(@PathVariable Long id, Principal principal) {
        User user = getAuthenticatedUser(principal);
        statsService.deleteResult(id, user);
        return ResponseEntity.ok(ApiResponse.success("Result deleted successfully", "DELETED"));
    }

    @DeleteMapping("/all")
    public ResponseEntity<ApiResponse<String>> clearAllResults(Principal principal) {
        User user = getAuthenticatedUser(principal);
        statsService.clearAllResults(user);
        return ResponseEntity.ok(ApiResponse.success("All test history cleared", "CLEARED"));
    }

    @DeleteMapping("/account")
    public ResponseEntity<ApiResponse<String>> deleteAccount(Principal principal, HttpServletRequest request) {
        User user = getAuthenticatedUser(principal);
        userService.deleteAccount(user);

        // Invalidate session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(ApiResponse.success("Account and associated data deleted successfully", "DELETED"));
    }
}
