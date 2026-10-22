package com.typeflow.controller;

import com.typeflow.dto.ApiResponse;
import com.typeflow.dto.PassageDto;
import com.typeflow.dto.TypingResultResponseDto;
import com.typeflow.dto.TypingSubmissionDto;
import com.typeflow.entity.User;
import com.typeflow.service.PassageService;
import com.typeflow.service.TypingService;
import com.typeflow.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/test")
public class TypingTestController {

    private final PassageService passageService;
    private final TypingService typingService;
    private final UserService userService;

    @Autowired
    public TypingTestController(PassageService passageService,
                                TypingService typingService,
                                UserService userService) {
        this.passageService = passageService;
        this.typingService = typingService;
        this.userService = userService;
    }

    @GetMapping("/passage")
    public ResponseEntity<ApiResponse<PassageDto>> getRandomPassage(
            @RequestParam(required = false) String excludeId) {
        PassageDto passage = passageService.getRandomPassage(excludeId);
        return ResponseEntity.ok(ApiResponse.success(passage));
    }

    @GetMapping("/passage/next")
    public ResponseEntity<ApiResponse<Map<String, String>>> getNextParagraph(
            @RequestParam(required = false) String currentId) {
        String nextText = passageService.getNextParagraph(currentId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("text", nextText)));
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<TypingResultResponseDto>> submitResult(
            @Valid @RequestBody TypingSubmissionDto submission,
            Principal principal) {
        User user = null;
        if (principal != null) {
            Optional<User> userOpt = userService.findByUsernameOrEmail(principal.getName());
            if (userOpt.isPresent()) {
                user = userOpt.get();
            }
        }

        TypingResultResponseDto result = typingService.processSubmission(submission, user);
        return ResponseEntity.ok(ApiResponse.success("Test completed and verified", result));
    }

    @GetMapping("/result/{id}")
    public ResponseEntity<ApiResponse<TypingResultResponseDto>> getResultById(@PathVariable Long id) {
        TypingResultResponseDto result = typingService.getResultById(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
