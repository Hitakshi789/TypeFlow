package com.typeflow.controller;

import com.typeflow.dto.ApiResponse;
import com.typeflow.dto.LeaderboardEntryDto;
import com.typeflow.entity.User;
import com.typeflow.service.LeaderboardService;
import com.typeflow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;
    private final UserService userService;

    @Autowired
    public LeaderboardController(LeaderboardService leaderboardService, UserService userService) {
        this.leaderboardService = leaderboardService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LeaderboardEntryDto>>> getLeaderboard(
            @RequestParam(required = false) Integer duration,
            Principal principal) {
        Long currentUserId = null;
        if (principal != null) {
            Optional<User> userOpt = userService.findByUsernameOrEmail(principal.getName());
            if (userOpt.isPresent()) {
                currentUserId = userOpt.get().getId();
            }
        }

        List<LeaderboardEntryDto> leaderboard = leaderboardService.get24HourLeaderboard(duration, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(leaderboard));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryDto>>> getRecentActivity(
            @RequestParam(defaultValue = "10") int limit) {
        List<LeaderboardEntryDto> recent = leaderboardService.getRecentActivity(limit);
        return ResponseEntity.ok(ApiResponse.success(recent));
    }

    @GetMapping("/user-rank")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserRank(
            @RequestParam(required = false) Integer duration,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.success(Map.of("rank", -1, "message", "User not logged in")));
        }
        Optional<User> userOpt = userService.findByUsernameOrEmail(principal.getName());
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(Map.of("rank", -1, "message", "User not found")));
        }
        Integer rank = leaderboardService.getUserRank(userOpt.get().getId(), duration);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "rank", rank != null ? rank : -1,
                "userId", userOpt.get().getId(),
                "username", userOpt.get().getUsername()
        )));
    }
}
