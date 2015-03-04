package com.flipkart.hydra.dispatcher;

import com.flipkart.hydra.task.Task;
import com.flipkart.hydra.task.exception.BadCallableException;
import com.flipkart.hydra.composer.IComposer;
import com.flipkart.hydra.dispatcher.exception.DispatchFailedException;
import com.flipkart.hydra.expression.exception.ExpressionEvaluationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Dispatcher implements IDispatcher {

    private final ExecutorCompletionService<Object> completionService;

    public Dispatcher() {
        this(Executors.newCachedThreadPool());
    }

    public Dispatcher(Executor executor) {
        completionService = new ExecutorCompletionService<>(executor);
    }

    @Override
    public Object execute(Map<String, Object> params, Map<String, Task> tasks, IComposer composer) throws DispatchFailedException, ExpressionEvaluationException {
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
                if (!dispatched.contains(key)) {
                    List<String> dependencies = task.getDependencies();
                    boolean dependenciesMet = true;
                    for (String dependency : dependencies) {
                        if (!responses.containsKey(dependency)) {
                            dependenciesMet = false;
                            break;
                        }
                    }

                    if (dependenciesMet) {
                        Future<Object> future = dispatchTask(task, responses);
                        dispatched.add(key);
                        futures.put(future, key);
                    }
                }
            }

            try {
                Future future = completionService.take();
                responses.put(futures.get(future), future.get());
                remaining--;
            } catch (InterruptedException | ExecutionException e) {
                throw new DispatchFailedException("Unable to fetch all required data", e);
            }
        }

        return responses;
    }

    private Future<Object> dispatchTask(Task task, Map<String, Object> responses) throws ExpressionEvaluationException, DispatchFailedException {
        try {
            IComposer composer = task.getComposer();
            Callable<Object> callable = task.getCallable(composer.compose(responses));
            return completionService.submit(callable);
        } catch (BadCallableException e) {
            throw new DispatchFailedException("Failed to dispatch task", e);
        }
    }
}
