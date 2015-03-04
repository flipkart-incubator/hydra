package com.flipkart.hydra.task;

import com.flipkart.hydra.task.exception.BadCallableException;
import com.flipkart.hydra.composer.IComposer;

import java.util.List;
import java.util.concurrent.Callable;

public interface Task {

    public IComposer getComposer();

    public Callable<Object> getCallable(Object values) throws BadCallableException;

    public List<String> getDependencies();
}
