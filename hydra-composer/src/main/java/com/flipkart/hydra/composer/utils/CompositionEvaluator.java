/**
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

package com.flipkart.hydra.composer.utils;

import com.flipkart.hydra.composer.exception.ComposerEvaluationException;
import com.flipkart.hydra.expression.Expression;
import com.flipkart.hydra.expression.exception.ExpressionEvaluationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompositionEvaluator {

    public static Object evaluate(Object context, Map<String, Object> values) throws ComposerEvaluationException {
        if (context instanceof Expression) {
            try {
                return ((Expression) context).calculate(values);
            } catch (ExpressionEvaluationException e) {
                throw new ComposerEvaluationException("Unable to evaluate composer.", e);
            }
        }

        if (context instanceof Map) {
            Map mapContext = (Map) context;
            Map<Object, Object> newMapContext = new HashMap<>();
            for (Object key : mapContext.keySet()) {
                newMapContext.put(evaluate(key, values), evaluate(mapContext.get(key), values));
            }
            context = newMapContext;
        } else if (context instanceof List) {
            List listContext = (List) context;
            List<Object> newListContext = new ArrayList<>();
            for (Object value : listContext) {
                newListContext.add(evaluate(value, values));
            }
            context = newListContext;
        }

        return context;
    }
}
