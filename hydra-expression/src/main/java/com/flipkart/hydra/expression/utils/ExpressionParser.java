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
import java.util.regex.Pattern;

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
            } else if (c == '[' || c == '(' || c == '.') {
                if ("".equals(token) || "$".equals(token)) {
                    if (c != '(') {
                        throw new ExpressionParseException(expression, "a-z A-Z 0-9 _ . [ (", c, i + 1);
                    }
                }

                if (token != "$") {
                    operands.add(token);
                }

                token = "";
                int brackets = 0;
                if (c == '[' || c == '(') {
                    char originalBracket = c;
                    char inverseBracket = (c == '[' ? ']' : ')');
                    for (; i < expression.length(); i++) {
                        c = expression.charAt(i);
                        switch (c) {
                            case '[':
                            case '(':
                                if (brackets != 0) {
                                    token += c;
                                }
                                brackets += (c == originalBracket ? 1 : 0);
                                break;
                            case ']':
                            case ')':
                                brackets -= (c == inverseBracket ? 1 : 0);
                                if (brackets != 0) {
                                    token += c;
                                }
                                break;
                            default:
                                token += c;
                        }

                        if (brackets == 0) {
                            switch (originalBracket) {
                                case '[':
                                    operands.add(parseSubExpression(expression, token, i + 1));
                                    break;
                                case '(':
                                    operands.add(parseFunction(expression, token, i + 1));
                                    break;
                            }

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
                throw new ExpressionParseException(expression, "a-z A-Z 0-9 _ . [ (", c, i + 1);
            }
        }

        if ("".equals(token) || "$".equals(token)) {
            if (c != ']' && c != ')') {
                throw new ExpressionParseException(expression, "a-z A-Z 0-9 _", "EOF", expression.length());
            }
        } else {
            operands.add(token);
        }

        return operands;
    }

    private static List<Object> parseFunction(String expression, String subExpression, int position) throws ExpressionParseException {
        try {
            List<Object> operands = new ArrayList<>();
            int bracketsA = 0;
            int bracketsB = 0;
            String token = "";
            for (int i = 0; i <= subExpression.length(); i++) {
                char c = (i == subExpression.length() ? ',' : subExpression.charAt(i));
                switch (c) {
                    case '[':
                        bracketsA++;
                        break;
                    case '(':
                        bracketsB++;
                        break;
                    case ']':
                        bracketsA--;
                        break;
                    case ')':
                        bracketsB--;
                        break;
                }

                if (c == ',') {
                    if (bracketsA == 0 && bracketsB == 0) {
                        if (operands.size() == 0) {
                            verifyFunctionName(token);
                            operands.add(token + "()");
                        } else {
                            operands.add(parseSubExpression(subExpression, token, i + 1));
                        }
                        token = "";
                    } else {
                        token += c;
                    }
                } else {
                    token += c;
                }
            }

            return operands;
        } catch (ExpressionParseException e) {
            throw new ExpressionParseException(expression, subExpression, position);
        }
    }

    private static void verifyFunctionName(String subExpression) throws ExpressionParseException {
        if (!Pattern.matches("[a-zA-Z][a-zA-Z0-9]*", subExpression)) {
            throw new ExpressionParseException("Invalid function name - " + subExpression);
        }
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
