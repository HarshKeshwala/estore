package com.estore.userservice.user.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ErrorResponse(
        int status,
        String message,
        Map<String, List<String>> errors,
        LocalDateTime timestamp
) {}

