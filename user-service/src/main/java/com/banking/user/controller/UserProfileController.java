package com.banking.user.controller;

import com.banking.user.dto.UserProfileRequest;
import com.banking.user.dto.UserProfileResponse;
import com.banking.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profiles", description = "User Profile Management APIs")
@SecurityRequirement(name = "bearerAuth")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PostMapping
    @Operation(summary = "Create user profile", description = "Creates a new user profile")
    public ResponseEntity<UserProfileResponse> createProfile(@Valid @RequestBody UserProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userProfileService.createProfile(request));
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get user profile by username")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable String username) {
        return ResponseEntity.ok(userProfileService.getProfileByUsername(username));
    }

    @GetMapping("/id/{id}")
    @Operation(summary = "Get user profile by ID")
    public ResponseEntity<UserProfileResponse> getProfileById(@PathVariable Long id) {
        return ResponseEntity.ok(userProfileService.getProfileById(id));
    }

    @GetMapping
    @Operation(summary = "Get all user profiles (Admin only)")
    public ResponseEntity<List<UserProfileResponse>> getAllProfiles() {
        return ResponseEntity.ok(userProfileService.getAllProfiles());
    }

    @PutMapping("/{username}")
    @Operation(summary = "Update user profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @PathVariable String username,
            @Valid @RequestBody UserProfileRequest request) {
        return ResponseEntity.ok(userProfileService.updateProfile(username, request));
    }

    @DeleteMapping("/{username}")
    @Operation(summary = "Deactivate user profile")
    public ResponseEntity<Void> deactivateProfile(@PathVariable String username) {
        userProfileService.deactivateProfile(username);
        return ResponseEntity.noContent().build();
    }
}
