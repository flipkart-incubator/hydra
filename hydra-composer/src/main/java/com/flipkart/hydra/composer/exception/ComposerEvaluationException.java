package com.flipkart.hydra.composer.exception;

public class ComposerEvaluationException extends Exception {

    public ComposerEvaluationException() {
        super();
    }

    public ComposerEvaluationException(String message) {
        super(message);
    }

    public ComposerEvaluationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ComposerEvaluationException(Throwable cause) {
        super(cause);
    }
}