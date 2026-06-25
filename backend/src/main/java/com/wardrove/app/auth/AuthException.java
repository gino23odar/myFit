package com.wardrove.app.auth;

public class AuthException extends RuntimeException {

    private final String error;

    public AuthException(String error, String message) {
        super(message);
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
