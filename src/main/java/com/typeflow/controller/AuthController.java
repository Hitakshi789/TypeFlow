package com.typeflow.controller;

import com.typeflow.dto.ApiResponse;
import com.typeflow.dto.AuthRequest;
import com.typeflow.dto.RegisterRequest;
import com.typeflow.dto.UserProfileDto;
import com.typeflow.entity.User;
import com.typeflow.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserProfileDto>> register(@Valid @RequestBody RegisterRequest request,
                                                               HttpServletRequest httpRequest) {
        User user = userService.registerUser(request);

        // Auto authenticate after registration
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
        );
        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(auth);
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);

        UserProfileDto profile = userService.getUserProfile(user);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", profile));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserProfileDto>> login(@Valid @RequestBody AuthRequest request,
                                                            HttpServletRequest httpRequest) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
        );
        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(auth);
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);

        Optional<User> userOpt = userService.findByUsernameOrEmail(request.getUsernameOrEmail());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User not found"));
        }

        UserProfileDto profile = userService.getUserProfile(userOpt.get());
        return ResponseEntity.ok(ApiResponse.success("Login successful", profile));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<UserProfileDto>> status(Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.success("Guest user", null));
        }
        Optional<User> userOpt = userService.findByUsernameOrEmail(principal.getName());
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("Guest user", null));
        }
        UserProfileDto profile = userService.getUserProfile(userOpt.get());
        return ResponseEntity.ok(ApiResponse.success("Authenticated", profile));
    }

    @PostMapping("/claim-guest")
    public ResponseEntity<ApiResponse<String>> claimGuestResult(@RequestBody Map<String, Long> payload,
                                                               Principal principal) {
        if (principal == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Must be logged in to claim results"));
        }
        Long resultId = payload.get("resultId");
        if (resultId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("resultId is required"));
        }
        Optional<User> userOpt = userService.findByUsernameOrEmail(principal.getName());
        userOpt.ifPresent(user -> userService.claimGuestResult(resultId, user));
        return ResponseEntity.ok(ApiResponse.success("Result claimed successfully", "OK"));
    }
}
