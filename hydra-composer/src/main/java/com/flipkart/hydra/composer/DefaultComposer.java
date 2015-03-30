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

package com.flipkart.hydra.composer;

import com.flipkart.hydra.composer.exception.ComposerEvaluationException;
import com.flipkart.hydra.composer.exception.ComposerInstantiationException;
import com.flipkart.hydra.expression.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.flipkart.hydra.composer.utils.CompositionEvaluator.evaluate;
import static com.flipkart.hydra.composer.utils.CompositionParser.parse;

public class DefaultComposer implements Composer {

    private final Object context;
    private final List<String> dependencies;

    public DefaultComposer(Object context) throws ComposerInstantiationException {
        this(context, false);
    }

    public DefaultComposer(Object context, boolean isAlreadyParsed) throws ComposerInstantiationException {
        this.context = isAlreadyParsed ? context : parse(context);
        this.dependencies = findDependencies(this.context);
    }

    @Override
    public Object compose(Map<String, Object> values) throws ComposerEvaluationException {
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
