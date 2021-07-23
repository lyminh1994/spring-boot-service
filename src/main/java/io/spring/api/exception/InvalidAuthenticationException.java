package io.spring.api.exception;

public class InvalidAuthenticationException extends RuntimeException {

    public InvalidAuthenticationException() {
        super("Invalid email or password");
    }
}
