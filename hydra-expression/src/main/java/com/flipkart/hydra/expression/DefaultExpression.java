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

package com.flipkart.hydra.expression;

import com.flipkart.hydra.expression.exception.ExpressionEvaluationException;
import com.flipkart.hydra.expression.exception.ExpressionParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.flipkart.hydra.expression.utils.ExpressionEvaluator.evaluate;
import static com.flipkart.hydra.expression.utils.ExpressionParser.parse;

public class DefaultExpression implements Expression {

    private final String expression;
    private final List operands;
    private final List<String> dependencies;
    private final boolean isOptional;

    public DefaultExpression(String expression) throws ExpressionParseException {
        expression = expression.replaceAll("\\s", "");

        this.isOptional = expression.startsWith("#");
        this.expression = expression.substring(isOptional ? 1 : 0);
        this.operands = parse(this.expression);
        this.dependencies = findDependencies(operands);
    }

    @Override
    public Object calculate(Map<String, Object> values) throws ExpressionEvaluationException {
        Object value = evaluate(operands, values);
        if (!isOptional && value == null) {
            throw new ExpressionEvaluationException("Null value found for non-optional expression - " + expression);
        }

        return value;
    }

    @Override
    public List<String> getDependencies() {
        return dependencies;
    }

    private List<String> findDependencies(List operands) {
        List<String> dependencies = new ArrayList<>();
        for (Object value : operands) {
            if (value instanceof List) {
                dependencies.addAll(findDependencies((List) value));
            } else if (value instanceof String) {
                String stringValue = (String) value;
                if (stringValue.startsWith("$") && !stringValue.startsWith("$__")) {
                    dependencies.add(stringValue.substring(1));
                }
            }
        }

        return dependencies;
    }
}
