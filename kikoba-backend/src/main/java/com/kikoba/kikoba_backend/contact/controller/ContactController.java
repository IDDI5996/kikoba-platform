package com.kikoba.kikoba_backend.contact.controller;

import com.kikoba.kikoba_backend.contact.entity.ContactMessage;
import com.kikoba.kikoba_backend.contact.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactMessageRepository repository;

    @PostMapping
    public ResponseEntity<?> submitMessage(@RequestBody ContactMessage request) {
        repository.save(request);
        return ResponseEntity.ok(Map.of("message", "Message saved successfully"));
    }
}