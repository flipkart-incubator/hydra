package com.flipkart.hydra.composer;

import com.flipkart.hydra.expression.Expression;
import com.flipkart.hydra.expression.exception.ExpressionParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompositionParser {

    public static Object parse(Object context) throws ExpressionParseException {
        if (context instanceof String) {
            String stringContext = (String) context;
            if (stringContext.startsWith("{{") && stringContext.endsWith("}}")) {
                String varString = stringContext.substring(2, stringContext.length() - 2);
                return new Expression(varString);
            }
        }

        if (context instanceof Map) {
            Map mapContext = (Map) context;
            Map<Object, Object> newMapContext = new HashMap<>();
            for (Object key : mapContext.keySet()) {
                newMapContext.put(parse(key), parse(mapContext.get(key)));
            }
            context = newMapContext;
        } else if (context instanceof List) {
            List listContext = (List) context;
            List<Object> newListContext = new ArrayList<>();
            for (Object value : listContext) {
                newListContext.add(parse(value));
            }
            context = newListContext;
        }

        return context;
    }
}
