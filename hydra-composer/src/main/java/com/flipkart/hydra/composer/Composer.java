package com.flipkart.hydra.composer;

import com.flipkart.hydra.expression.exception.ExpressionEvaluationException;

import java.util.List;
import java.util.Map;

public interface Composer {

    public Object compose(Map<String, Object> values) throws ExpressionEvaluationException;

    public List<String> getDependencies();
}
