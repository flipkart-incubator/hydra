package com.flipkart.hydra.expression;

import com.flipkart.hydra.expression.exception.ExpressionEvaluationException;
import com.flipkart.hydra.expression.exception.ExpressionParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.flipkart.hydra.expression.ExpressionEvaluator.evaluate;
import static com.flipkart.hydra.expression.ExpressionParser.parse;

public class Expression implements IExpression {

    private final String expression;
    private final List operands;
    private final List<String> dependencies;
    private final boolean isOptional;

    public Expression(String expression) throws ExpressionParseException {
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
                if (stringValue.startsWith("$")) {
                    dependencies.add(stringValue.substring(1));
                }
            }
        }

        return dependencies;
    }
}
