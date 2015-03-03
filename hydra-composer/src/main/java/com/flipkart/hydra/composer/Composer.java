package com.flipkart.hydra.composer;

import com.flipkart.hydra.expression.IExpression;
import com.flipkart.hydra.expression.exception.ExpressionEvaluationException;
import com.flipkart.hydra.expression.exception.ExpressionParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.flipkart.hydra.composer.CompositionEvaluator.evaluate;
import static com.flipkart.hydra.composer.CompositionParser.parse;

public class Composer implements IComposer {

    private final Object context;
    private final List<String> dependencies;

    public Composer(Object context) throws ExpressionParseException {
        this(context, false);
    }

    public Composer(Object context, boolean isAlreadyParsed) throws ExpressionParseException {
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
        if (context instanceof IExpression) {
            dependencies.addAll(((IExpression) context).getDependencies());
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
