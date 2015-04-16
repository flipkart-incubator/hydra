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
import com.flipkart.hydra.task.exception.BadCallableException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.flipkart.hydra.task.helpers.ReflectionHelper.getFirstSingleArgConstructor;

public class DefaultTask implements Task {

    protected final Class<? extends Callable> callableClass;
    protected final Composer composer;

    public DefaultTask(Class<? extends Callable> callableClass, Composer composer) {
        this.callableClass = callableClass;
        this.composer = composer;
    }

    public DefaultTask(Class<? extends Callable> callableClass, Object context) throws ComposerInstantiationException {
        this(callableClass, context, false);
    }

    public DefaultTask(Class<? extends Callable> callableClass, Object context, boolean isAlreadyParsed) throws ComposerInstantiationException {
        this.callableClass = callableClass;
        this.composer = new DefaultComposer(context, isAlreadyParsed);
    }

    @Override
    public Callable<Object> getCallable(Map<String, Object> values) throws BadCallableException {
        try {
            Constructor<? extends Callable> constructor = getFirstSingleArgConstructor(callableClass);
            return (Callable<Object>) constructor.newInstance(composer.compose(values));
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | ComposerEvaluationException e) {
            throw new BadCallableException("Unable to execute callable", e);
        }
    }

    @Override
    public List<String> getDependencies() {
        return composer.getDependencies();
    }
}
