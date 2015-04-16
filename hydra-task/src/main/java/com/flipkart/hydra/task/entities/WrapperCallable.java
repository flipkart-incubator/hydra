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

package com.flipkart.hydra.task.entities;

import com.flipkart.hydra.composer.Composer;
import com.flipkart.hydra.composer.exception.ComposerEvaluationException;
import com.flipkart.hydra.task.exception.BadCallableException;
import com.flipkart.hydra.task.helpers.ReflectionHelper;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class WrapperCallable implements Callable<Object> {

    private final ListeningExecutorService executorService;
    private final Constructor<? extends Callable> constructor;
    private final Object loopVar;
    private final Composer composer;
    private final Map<String, Object> values;

    public WrapperCallable(ExecutorService executor, Class<? extends Callable> callableClass, Composer loopComposer, Composer composer, Map<String, Object> values) throws NoSuchMethodException, ComposerEvaluationException {
        this.executorService = MoreExecutors.listeningDecorator(executor);
        this.constructor = ReflectionHelper.getFirstSingleArgConstructor(callableClass);
        this.loopVar = loopComposer.compose(values);
        this.composer = composer;
        this.values = values;
    }

    @Override
    public Object call() throws Exception {
        try {
            if (loopVar instanceof Map) {
                return getResponsesForMap();
            } else if (loopVar instanceof List) {
                return getResponsesForList();
            } else if (loopVar.getClass().isArray()) {
                return getResponsesForArray();
            }
        } catch (Throwable e) {
            throw new BadCallableException("Unable to execute callable", e);
        }

        throw new BadCallableException("Loop Variable is not iterable");
    }

    private Object getResponsesForMap() throws InvocationTargetException, InstantiationException, ComposerEvaluationException, IllegalAccessException, ExecutionException, InterruptedException {
        Map iterable = (Map) loopVar;

        Map<Object, ListenableFuture<Object>> futureMap = new HashMap<>();
        for (Object i : iterable.keySet()) {
            ListenableFuture<Object> future = getFuture(i, iterable.get(i));
            futureMap.put(i, future);
        }

        ListenableFuture<List<Object>> compositeFuture = Futures.allAsList(futureMap.values());
        compositeFuture.get();

        Map<Object, Object> responsesMap = new HashMap<>();
        for (Object i : iterable.keySet()) {
            responsesMap.put(i, futureMap.get(i).get());
        }

        return responsesMap;
    }

    private Object getResponsesForList() throws InvocationTargetException, InstantiationException, ComposerEvaluationException, IllegalAccessException, ExecutionException, InterruptedException {
        List list = (List) loopVar;

        Map<Integer, ListenableFuture<Object>> futureMap = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            ListenableFuture<Object> future = getFuture(i, list.get(i));
            futureMap.put(i, future);
        }

        ListenableFuture<List<Object>> compositeFuture = Futures.allAsList(futureMap.values());
        compositeFuture.get();

        List<Object> responsesList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            responsesList.add(futureMap.get(i));
        }

        return responsesList;
    }

    private Object getResponsesForArray() throws InvocationTargetException, InstantiationException, ComposerEvaluationException, IllegalAccessException, ExecutionException, InterruptedException {
        Object[] arr = (Object[]) loopVar;

        Map<Integer, ListenableFuture<Object>> futureMap = new HashMap<>();
        for (int i = 0; i < arr.length; i++) {
            ListenableFuture<Object> future = getFuture(i, arr[i]);
            futureMap.put(i, future);
        }

        ListenableFuture<List<Object>> compositeFuture = Futures.allAsList(futureMap.values());
        compositeFuture.get();

        Object[] responsesArray = new Object[arr.length];
        for (int i = 0; i < arr.length; i++) {
            responsesArray[i] = futureMap.get(i);
        }

        return responsesArray;
    }

    private ListenableFuture<Object> getFuture(Object key, Object value) throws InstantiationException, IllegalAccessException, InvocationTargetException, ComposerEvaluationException {
        Map<String, Object> request = new HashMap<>(values);
        request.put("__key", key);
        request.put("__value", value);
        Callable<Object> callable = (Callable<Object>) constructor.newInstance(composer.compose(request));
        return executorService.submit(callable);
    }
}
