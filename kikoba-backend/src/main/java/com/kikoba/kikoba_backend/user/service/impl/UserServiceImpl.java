package com.kikoba.kikoba_backend.user.service.impl;

import com.kikoba.kikoba_backend.exception.ResourceAlreadyExistsException;
import com.kikoba.kikoba_backend.exception.ResourceNotFoundException;
import com.kikoba.kikoba_backend.exception.BadRequestException;
import com.kikoba.kikoba_backend.user.dto.RegisterRequest;
import com.kikoba.kikoba_backend.user.dto.UpdateProfileRequest;
import com.kikoba.kikoba_backend.user.dto.UserResponse;
import com.kikoba.kikoba_backend.user.entity.User;
import com.kikoba.kikoba_backend.user.mapper.UserMapper;
import com.kikoba.kikoba_backend.user.repository.UserRepository;
import com.kikoba.kikoba_backend.user.service.FileStorageService;
import com.kikoba.kikoba_backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        log.info("Registering new user with phone: {}", request.getPhoneNumber());

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ResourceAlreadyExistsException("Phone number already registered");
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already registered");
        }

        User user = User.builder()
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .preferredLanguage("en")
                .isActive(true)
                .kycVerified(false)
                .mfaEnabled(false)
                .build();

        User saved = userRepository.save(user);
        log.info("User registered successfully: {}", saved.getId());

        return userMapper.toResponse(saved);
    }

    @Override
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getUserByPhone(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with phone: " + phoneNumber));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ResourceAlreadyExistsException("Email already registered");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPreferredLanguage() != null) {
            user.setPreferredLanguage(request.getPreferredLanguage());
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse enableMfa(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setMfaEnabled(true);
        User saved = userRepository.save(user);
        log.info("MFA enabled for user: {}", userId);
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse disableMfa(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        User saved = userRepository.save(user);
        log.info("MFA disabled for user: {}", userId);
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse updateAvatar(UUID userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String url = fileStorageService.saveAvatar(userId, file);
        user.setProfilePictureUrl(url);
        User saved = userRepository.save(user);
        log.info("Avatar updated for user: {}", userId);
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void verifyKyc(UUID userId, String documentUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setKycVerified(true);
        user.setKycDocumentUrl(documentUrl);
        user.setKycVerifiedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("KYC verified for user: {}", userId);
    }

    @Override
    @Transactional
    public void deactivateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setIsActive(false);
        userRepository.save(user);
        log.info("User deactivated: {}", userId);
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 1. Verify current password matches the database hash
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BadRequestException("Incorrect current password");
        }

        // 2. Prevent using the same password
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new BadRequestException("New password cannot be the same as the old password");
        }

        // 3. Save new password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", userId);
    }
}