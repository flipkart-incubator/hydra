package com.flipkart.hydra.expression.utils;

import com.flipkart.hydra.expression.exception.ExpressionEvaluationException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class ExpressionEvaluator {

    public static Object evaluate(List operands, Map<String, Object> values) throws ExpressionEvaluationException {
        Object response = null;
        for (Object operand : operands) {
            Object calculatedOperand = operand;
            if (operand instanceof List) {
                calculatedOperand = evaluate((List) operand, values);
            }

            response = calculateOperandResponse(calculatedOperand, values, response);
            if (response == null) {
                break;
            }
        }

        return response;
    }

    private static Object calculateOperandResponse(Object operand, Map<String, Object> values, Object response) throws ExpressionEvaluationException {
        if (response == null) {
            if (operand instanceof String) {
                String stringOperand = (String) operand;
                if (stringOperand.startsWith("$")) {
                    String variable = stringOperand.substring(1);
                    response = values.get(variable);
                }
            } else {
                response = operand;
            }
        } else {
            if (response instanceof List) {
                if (operand instanceof Integer) {
                    response = ((List) response).get((Integer) operand);
                } else {
                    throw new ExpressionEvaluationException("Invalid operand - " + operand);
                }
            } else if (response instanceof Map) {
                Object possibleResponse = ((Map) response).get(operand);
                response = possibleResponse != null ? possibleResponse : ((Map) response).get(String.valueOf(operand));
            } else if (response instanceof String) {
                if (operand instanceof Integer) {
                    response = ((String) response).charAt((Integer) operand);
                } else {
                    throw new ExpressionEvaluationException("Invalid operand - " + operand);
                }
            } else if (response instanceof Integer || response instanceof Double) {
                throw new ExpressionEvaluationException("Invalid operand - " + operand);
            } else if (!(operand instanceof String)) {
                throw new ExpressionEvaluationException("Invalid operand - " + operand);
            } else {
                response = getFieldValue((String) operand, response);
            }
        }

        return response;
    }

    private static Object getFieldValue(String operand, Object response) throws ExpressionEvaluationException {
        Class<?> klass = response.getClass();
        while (klass != null && klass != Object.class) {
            Field[] declaredFields = klass.getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.getName().equals(operand)) {
                    try {
                        Field declaredField = klass.getDeclaredField(operand);
                        declaredField.setAccessible(true);
                        return declaredField.get(response);
                    } catch (IllegalAccessException | NoSuchFieldException ignored) {
                    }
                }
            }

            klass = klass.getSuperclass();
        }

        throw new ExpressionEvaluationException("No field named '" + operand + "' exists for " + response.getClass().getName());
    }
}
