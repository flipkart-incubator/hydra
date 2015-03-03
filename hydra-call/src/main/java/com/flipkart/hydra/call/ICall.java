package com.flipkart.hydra.call;

import com.flipkart.hydra.call.exception.BadCallableException;
import com.flipkart.hydra.composer.IComposer;

import java.util.List;
import java.util.concurrent.Callable;

public interface ICall {

    public IComposer getComposer();

    public Callable<Object> getCallable(Object values) throws BadCallableException;

    public List<String> getDependencies();
}
