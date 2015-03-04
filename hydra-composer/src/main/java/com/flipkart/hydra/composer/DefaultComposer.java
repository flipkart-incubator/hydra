package com.flipkart.hydra.composer;

import com.flipkart.hydra.expression.Expression;
import com.flipkart.hydra.expression.exception.ExpressionEvaluationException;
import com.flipkart.hydra.expression.exception.ExpressionParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.flipkart.hydra.composer.utils.CompositionEvaluator.evaluate;
import static com.flipkart.hydra.composer.utils.CompositionParser.parse;

public class DefaultComposer implements Composer {

    private final Object context;
    private final List<String> dependencies;

    public DefaultComposer(Object context) throws ExpressionParseException {
        this(context, false);
    }

    public DefaultComposer(Object context, boolean isAlreadyParsed) throws ExpressionParseException {
        this.context = isAlreadyParsed ? context : parse(context);
        this.dependencies = findDependencies(this.context);
    }

    @Override
    public Object compose(Map<String, Object> values) throws ExpressionEvaluationException {
        return evaluate(context, values);
    }

    @Override
    public List<String> getDependencies() {
        return dependencies;
    }

    private List<String> findDependencies(Object context) {
        List<String> dependencies = new ArrayList<>();
        if (context instanceof Expression) {
            dependencies.addAll(((Expression) context).getDependencies());
        } else if (context instanceof Map) {
            Map mapContext = (Map) context;
            for (Object value : mapContext.values()) {
                dependencies.addAll(findDependencies(value));
            }
        } else if (context instanceof List) {
            List listContext = (List) context;
            for (Object value : listContext) {
                dependencies.addAll(findDependencies(value));
            }
        }

        return dependencies;
    }
}
