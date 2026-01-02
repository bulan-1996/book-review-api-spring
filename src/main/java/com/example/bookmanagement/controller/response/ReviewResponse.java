package com.example.bookmanagement.controller.response;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        String content,
        int rating,
        LocalDateTime createdAt
    ) {}
