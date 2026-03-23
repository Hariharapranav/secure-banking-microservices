package com.banking.user.service;

import com.banking.user.dto.UserProfileRequest;
import com.banking.user.dto.UserProfileResponse;
import com.banking.user.entity.UserProfile;
import com.banking.user.exception.ResourceNotFoundException;
import com.banking.user.exception.DuplicateResourceException;
import com.banking.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserProfileRepository repository;

    @Transactional
    public UserProfileResponse createProfile(UserProfileRequest request) {
        log.info("Creating user profile for: {}", request.getUsername());

        if (repository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Profile already exists for username: " + request.getUsername());
        }

        if (repository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Profile already exists for email: " + request.getEmail());
        }

        UserProfile profile = UserProfile.builder()
                .username(request.getUsername())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .dateOfBirth(request.getDateOfBirth())
                .panNumber(request.getPanNumber())
                .aadhaarNumber(request.getAadhaarNumber())
                .kycStatus(UserProfile.KycStatus.PENDING)
                .active(true)
                .build();

        profile = repository.save(profile);
        log.info("User profile created with id: {}", profile.getId());

        return UserProfileResponse.fromEntity(profile);
    }

    @Cacheable(value = "userProfiles", key = "#username")
    public UserProfileResponse getProfileByUsername(String username) {
        log.info("Fetching user profile for: {}", username);
        UserProfile profile = repository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found: " + username));
        return UserProfileResponse.fromEntity(profile);
    }

    @Cacheable(value = "userProfiles", key = "#id")
    public UserProfileResponse getProfileById(Long id) {
        log.info("Fetching user profile with id: {}", id);
        UserProfile profile = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found with id: " + id));
        return UserProfileResponse.fromEntity(profile);
    }

    public List<UserProfileResponse> getAllProfiles() {
        log.info("Fetching all user profiles");
        return repository.findAll().stream()
                .map(UserProfileResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "userProfiles", key = "#username")
    public UserProfileResponse updateProfile(String username, UserProfileRequest request) {
        log.info("Updating user profile for: {}", username);

        UserProfile profile = repository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found: " + username));

        profile.setFullName(request.getFullName());
        profile.setPhone(request.getPhone());
        profile.setAddress(request.getAddress());
        profile.setCity(request.getCity());
        profile.setState(request.getState());
        profile.setZipCode(request.getZipCode());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setPanNumber(request.getPanNumber());
        profile.setAadhaarNumber(request.getAadhaarNumber());

        profile = repository.save(profile);
        log.info("User profile updated for: {}", username);

        return UserProfileResponse.fromEntity(profile);
    }

    @Transactional
    @CacheEvict(value = "userProfiles", key = "#username")
    public void deactivateProfile(String username) {
        log.info("Deactivating user profile: {}", username);
        UserProfile profile = repository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found: " + username));
        profile.setActive(false);
        repository.save(profile);
    }
}
