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

package com.flipkart.hydra.dispatcher;

import com.flipkart.hydra.composer.Composer;
import com.flipkart.hydra.composer.DefaultComposer;
import com.flipkart.hydra.composer.exception.ComposerEvaluationException;
import com.flipkart.hydra.composer.exception.ComposerInstantiationException;
import com.flipkart.hydra.dispatcher.exception.DispatchFailedException;
import com.flipkart.hydra.task.Task;
import com.flipkart.hydra.task.exception.BadCallableException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class DefaultDispatcher implements Dispatcher {

    private final ExecutorService executor;
    private final ExecutorCompletionService<Object> completionService;

    public DefaultDispatcher() {
        this(Executors.newCachedThreadPool());
    }

    public DefaultDispatcher(ExecutorService executor) {
        this.executor = executor;
        completionService = new ExecutorCompletionService<>(executor);
    }

    @Override
    public Object execute(Map<String, Object> params, Map<String, Task> tasks, Object context) throws DispatchFailedException, ComposerEvaluationException {
        return execute(params, tasks, context, false);
    }

    @Override
    public Object execute(Map<String, Object> params, Map<String, Task> tasks, Object context, boolean isAlreadyParsed) throws DispatchFailedException, ComposerEvaluationException {
        try {
            DefaultComposer defaultComposer = new DefaultComposer(context, isAlreadyParsed);
            return execute(params, tasks, defaultComposer);
        } catch (ComposerInstantiationException e) {
            throw new DispatchFailedException("Unable to create composer.", e);
        }
    }

    @Override
    public Object execute(Map<String, Object> params, Map<String, Task> tasks, Composer composer) throws DispatchFailedException, ComposerEvaluationException {
        Map<String, Object> responses = dispatchAndCollect(params, tasks);

        List<String> dependencies = composer.getDependencies();
        Map<String, Object> collectedDependencies = collectDependencies(responses, dependencies);
        return composer.compose(collectedDependencies);
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }

    private Map<String, Object> dispatchAndCollect(Map<String, Object> params, Map<String, Task> tasks) throws DispatchFailedException, ComposerEvaluationException {
        Map<String, Object> responses = new HashMap<>();
        List<String> dispatched = new ArrayList<>();
        Map<Future<Object>, String> futures = new HashMap<>();

        responses.putAll(params);

        int remaining = tasks.size();
        while (remaining > 0) {
            for (String key : tasks.keySet()) {
                Task task = tasks.get(key);
                if (!responses.containsKey(key) && !dispatched.contains(key)) {
                    List<String> dependencies = task.getDependencies();
                    Map<String, Object> collectedDependencies = collectDependencies(responses, dependencies);
                    if (collectedDependencies.size() == dependencies.size()) {
                        Future<Object> future = dispatchTask(task, collectedDependencies);
                        dispatched.add(key);
                        futures.put(future, key);
                    }
                }
            }

            if (dispatched.isEmpty()) {
                throw new DispatchFailedException("No possible resolution of dependencies found.");
            }

            try {
                Future future = completionService.take();
                String key = futures.get(future);
                responses.put(key, future.get());
                dispatched.remove(key);
                remaining--;
            } catch (InterruptedException | ExecutionException e) {
                throw new DispatchFailedException("Unable to fetch all required data", e);
            }
        }

        return responses;
    }

    private Map<String, Object> collectDependencies(Map<String, Object> responses, List<String> dependencies) {
        Map<String, Object> collectedDependencies = new HashMap<>();
        for (String dependency : dependencies) {
            if (responses.containsKey(dependency)) {
                collectedDependencies.put(dependency, responses.get(dependency));
            }
        }

        return collectedDependencies;
    }

    private Future<Object> dispatchTask(Task task, Map<String, Object> responses) throws DispatchFailedException, ComposerEvaluationException {
        try {
            Composer composer = task.getComposer();
            Callable<Object> callable = task.getCallable(composer.compose(responses));
            return completionService.submit(callable);
        } catch (BadCallableException e) {
            throw new DispatchFailedException("Failed to dispatch task", e);
        }
    }
}
