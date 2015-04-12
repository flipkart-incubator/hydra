/*
 * Copyright 2015 Flipkart Internet, pvt ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.hydra.expression.utils;

import com.flipkart.hydra.expression.exception.ExpressionEvaluationException;
import com.google.common.base.Joiner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExpressionEvaluator {

    public static Object evaluate(List operands, Map<String, Object> values) throws ExpressionEvaluationException {
        Object response = null;

        Object firstOperand = operands.get(0);
        if (firstOperand instanceof String) {
            if (((String) firstOperand).endsWith("()")) {
                List arguments = operands.size() > 1 ? operands.subList(1, operands.size()) : new ArrayList();
                return evaluateFunction((String) firstOperand, arguments, values);
            }
        }

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

    private static Object evaluateFunction(String fn, List args, Map<String, Object> values) throws ExpressionEvaluationException {
        try {
            String function = fn.substring(0, fn.length() - 2);
            List computedArgs = new ArrayList();
            for (Object arg : args) {
                computedArgs.add(evaluate((List) arg, values));
            }

            return findFunction(function, computedArgs).invoke(null, computedArgs.toArray());
        } catch (Throwable e) {
            throw new ExpressionEvaluationException("Unable to evaluate function - " + fn, e);
        }
    }

    private static Method findFunction(String name, List args) throws ExpressionEvaluationException {
        Method[] methods = Functions.class.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(name) && method.getParameterCount() == args.size()) {
                boolean match = true;
                Class[] types = method.getParameterTypes();
                for (int i = 0; i < types.length; i++) {
                    if (!types[i].isInstance(args.get(i))) {
                        match = false;
                        break;
                    }
                }

                if (match) {
                    return method;
                }
            }
        }

        String[] typeNames = new String[args.size()];
        for (int i = 0; i < args.size(); i++) {
            Class type = args.get(i).getClass();
            typeNames[i] = type.getSimpleName();
            if (typeNames[i].equals("")) {
                typeNames[i] = type.getSuperclass().getSimpleName();
            }
        }

        throw new ExpressionEvaluationException("No such function found :: " + name + "(" + Joiner.on(", ").join(typeNames) + ")");
    }

    private static Object calculateOperandResponse(Object operand, Map<String, Object> values, Object response) throws ExpressionEvaluationException {
        if (response == null) {
            if (operand instanceof String) {
                String stringOperand = (String) operand;
                if (stringOperand.startsWith("$")) {
                    String variable = stringOperand.substring(1);
                    response = values.get(variable);
                } else {
                    response = operand;
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
            } else if (response.getClass().isArray()) {
                if (operand instanceof Integer) {
                    response = ((Object[]) response)[(Integer) operand];
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
