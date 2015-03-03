package com.flipkart.hydra.dispatcher.exception;

public class DispatchFailedException extends Exception {

    public DispatchFailedException() {
        super();
    }

    public DispatchFailedException(String message) {
        super(message);
    }

    public DispatchFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public DispatchFailedException(Throwable cause) {
        super(cause);
    }
}