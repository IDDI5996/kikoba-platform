package com.kikoba.kikoba_backend.user.repository;

import com.kikoba.kikoba_backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    // Method for Admin User Management search
    Page<User> findByPhoneNumberContainingOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String phoneNumber, String firstName, String lastName, Pageable pageable);
}