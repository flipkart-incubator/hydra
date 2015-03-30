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

import com.flipkart.hydra.expression.exception.ExpressionParseException;

import java.util.ArrayList;
import java.util.List;

public class ExpressionParser {

    public static List parse(final String expression) throws ExpressionParseException {
        if (expression.startsWith("$")) {
            return parseExpression(expression);
        } else {
            return new ArrayList<Object>() {{
                add(getValue(expression));
            }};
        }
    }

    private static List parseExpression(String expression) throws ExpressionParseException {
        List<Object> operands = new ArrayList<>();
        String token = "$";
        char c = '~';
        for (int i = 1; i < expression.length(); i++) {
            c = expression.charAt(i);
            if (isAllowedChar(c)) {
                token += c;
            } else if (c == '[' || c == '.') {
                if ("".equals(token) || "$".equals(token)) {
                    throw new ExpressionParseException(expression, "a-z A-Z 0-9 _ . [", c, i + 1);
                }

                operands.add(token);
                token = "";
                int brackets = 0;
                if (c == '[') {
                    for (; i < expression.length(); i++) {
                        c = expression.charAt(i);
                        switch (c) {
                            case '[':
                                brackets++;
                                break;
                            case ']':
                                brackets--;
                                break;
                            default:
                                token += c;
                        }

                        if (brackets == 0) {
                            operands.add(parseSubExpression(expression, token, i + 1));
                            token = "";
                            break;
                        }
                    }

                    i++;
                    if (i < expression.length()) {
                        c = expression.charAt(i);
                        if (c != '.') {
                            throw new ExpressionParseException(expression, ".", c, i + 1);
                        }
                    }
                }
            } else {
                throw new ExpressionParseException(expression, "a-z A-Z 0-9 _ . [ ", c, i + 1);
            }
        }

        if ("".equals(token) || "$".equals(token)) {
            if (c != ']') {
                throw new ExpressionParseException(expression, "a-z A-Z 0-9 _", "EOF", expression.length());
            }
        } else {
            operands.add(token);
        }

        return operands;
    }

    private static List parseSubExpression(String expression, String subExpression, int position) throws ExpressionParseException {
        try {
            return parse(subExpression);
        } catch (ExpressionParseException e) {
            throw new ExpressionParseException(expression, subExpression, position);
        }
    }

    private static Object getValue(String expression) {
        try {
            return Integer.parseInt(expression);
        } catch (NumberFormatException e1) {
            try {
                return Double.parseDouble(expression);
            } catch (NumberFormatException e2) {
                return expression;
            }
        }
    }

    private static boolean isAllowedChar(char c) {
        if (c >= 'a' && c <= 'z') {
            return true;
        } else if (c >= 'A' && c <= 'Z') {
            return true;
        } else if (c >= '0' && c <= '9') {
            return true;
        } else if (c == '_') {
            return true;
        }

        return false;
    }
}
