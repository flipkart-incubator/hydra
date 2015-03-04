package com.flipkart.hydra.dispatcher;

import com.flipkart.hydra.composer.Composer;
import com.flipkart.hydra.dispatcher.exception.DispatchFailedException;
import com.flipkart.hydra.expression.exception.ExpressionEvaluationException;
import com.flipkart.hydra.task.Task;
import com.flipkart.hydra.task.exception.BadCallableException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class DefaultDispatcher implements Dispatcher {

    private final ExecutorCompletionService<Object> completionService;

    public DefaultDispatcher() {
        this(Executors.newCachedThreadPool());
    }

    public DefaultDispatcher(Executor executor) {
        completionService = new ExecutorCompletionService<>(executor);
    }

    @Override
    public Object execute(Map<String, Object> params, Map<String, Task> tasks, Composer composer) throws DispatchFailedException, ExpressionEvaluationException {
        Map<String, Object> responses = dispatchAndCollect(params, tasks);
        return composer.compose(responses);
    }

    private Map<String, Object> dispatchAndCollect(Map<String, Object> params, Map<String, Task> tasks) throws DispatchFailedException, ExpressionEvaluationException {
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
                        Future<Object> future = dispatchTask(task, responses);
                        dispatched.add(key);
                        futures.put(future, key);
                    }
                }
            }

            if(dispatched.isEmpty()) {
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

    private Future<Object> dispatchTask(Task task, Map<String, Object> responses) throws ExpressionEvaluationException, DispatchFailedException {
        try {
            Composer composer = task.getComposer();
            Callable<Object> callable = task.getCallable(composer.compose(responses));
            return completionService.submit(callable);
        } catch (BadCallableException e) {
            throw new DispatchFailedException("Failed to dispatch task", e);
        }
    }
}
