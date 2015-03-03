package com.flipkart.hydra.expression.exception;

public class ExpressionParseException extends Exception {

    public ExpressionParseException() {
        super();
    }

    public ExpressionParseException(String message) {
        super(message);
    }

    public ExpressionParseException(String expression, String expected, char found, int position) {
        this(expression, expected, "" + found, position);
    }

    public ExpressionParseException(String expression, String expected, String found, int position) {
        super("Expected " + expected + ", Found '" + found + "' at position " + position + ". (Expression = " + expression + ")");
    }

    public ExpressionParseException(String expression, String subExpression, int position) {
        super("Unable to parse sub-expression '" + subExpression + "' starting at position " + position + ". (Expression = " + expression + ")");
    }

    public ExpressionParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExpressionParseException(Throwable cause) {
        super(cause);
    }
}