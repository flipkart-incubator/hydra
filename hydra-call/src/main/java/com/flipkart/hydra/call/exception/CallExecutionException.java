package com.flipkart.hydra.call.exception;

public class CallExecutionException extends Exception {

    public CallExecutionException() {
        super();
    }

    public CallExecutionException(String message) {
        super(message);
    }

    public CallExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public CallExecutionException(Throwable cause) {
        super(cause);
    }
}