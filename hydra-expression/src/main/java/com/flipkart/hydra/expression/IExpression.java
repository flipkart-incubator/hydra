package com.flipkart.hydra.expression;

import com.flipkart.hydra.expression.exception.ExpressionEvaluationException;

import java.util.List;
import java.util.Map;

public interface IExpression {

    public Object calculate(Map<String, Object> values) throws ExpressionEvaluationException;

    public List<String> getDependencies();
}
