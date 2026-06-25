package com.wardrove.app.common;

import java.time.Instant;

public record ApiErrorResponse(
        String error,
        String message,
        int status,
        Instant timestamp
) {
}
