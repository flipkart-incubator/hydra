package com.flipkart.hydra.composer.exception;

public class ComposerInstantiationException extends Exception {

    public ComposerInstantiationException() {
        super();
    }

    public ComposerInstantiationException(String message) {
        super(message);
    }

    public ComposerInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ComposerInstantiationException(Throwable cause) {
        super(cause);
    }
}