package com.splito.exception;

public class TokenReuseException extends UnauthorizedException {
    public TokenReuseException(String message) {
        super(message);
    }
}
