package com.flipkart.hydra.call.exception;

public class BadCallableException extends Exception {

    public BadCallableException() {
        super();
    }

    public BadCallableException(String message) {
        super(message);
    }

    public BadCallableException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadCallableException(Throwable cause) {
        super(cause);
    }
}