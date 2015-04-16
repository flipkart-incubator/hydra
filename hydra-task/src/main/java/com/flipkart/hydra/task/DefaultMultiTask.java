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

package com.flipkart.hydra.task;

import com.flipkart.hydra.composer.Composer;
import com.flipkart.hydra.composer.DefaultComposer;
import com.flipkart.hydra.composer.exception.ComposerEvaluationException;
import com.flipkart.hydra.composer.exception.ComposerInstantiationException;
import com.flipkart.hydra.task.entities.WrapperCallable;
import com.flipkart.hydra.task.exception.BadCallableException;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class DefaultMultiTask extends DefaultTask {

    private final Composer loopComposer;
    private final ExecutorService executor;

    public DefaultMultiTask(ExecutorService executor, Class<? extends Callable> callableClass, Object loopOverContext, Object context) throws ComposerInstantiationException {
        this(executor, callableClass, context, loopOverContext, false);
    }

    public DefaultMultiTask(ExecutorService executor, Class<? extends Callable> callableClass, Object context, Object loopOverContext, boolean isAlreadyParsed) throws ComposerInstantiationException {
        this(executor, callableClass, new DefaultComposer(context, isAlreadyParsed), new DefaultComposer(loopOverContext, isAlreadyParsed));
    }

    public DefaultMultiTask(ExecutorService executor, Class<? extends Callable> callableClass, Composer composer, Composer loopComposer) {
        super(callableClass, composer);
        this.executor = executor;
        this.loopComposer = loopComposer;
    }

    @Override
    public Callable<Object> getCallable(Map<String, Object> values) throws BadCallableException {
        if (loopComposer == null) {
            return super.getCallable(values);
        } else {
            try {
                return new WrapperCallable(executor, callableClass, loopComposer, composer, values);
            } catch (NoSuchMethodException | ComposerEvaluationException e) {
                throw new BadCallableException("Unable to execute callable", e);
            }
        }
    }

    @Override
    public List<String> getDependencies() {
        Set<String> dependencies = new HashSet<>(super.getDependencies());
        if (loopComposer != null) {
            dependencies.addAll(loopComposer.getDependencies());
        }

        return new ArrayList<>(dependencies);
    }
}
