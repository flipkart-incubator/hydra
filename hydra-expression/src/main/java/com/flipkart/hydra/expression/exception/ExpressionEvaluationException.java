package com.flipkart.hydra.expression.exception;

public class ExpressionEvaluationException extends Exception {

    public ExpressionEvaluationException() {
        super();
    }

    public ExpressionEvaluationException(String message) {
        super(message);
    }

    public ExpressionEvaluationException(String expected, String found) {
        super("Expected " + expected + ", Found " + found);
    }

    public ExpressionEvaluationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExpressionEvaluationException(Throwable cause) {
        super(cause);
    }
}