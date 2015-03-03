package com.flipkart.hydra.call;

import com.flipkart.hydra.call.exception.BadCallableException;
import com.flipkart.hydra.composer.Composer;
import com.flipkart.hydra.composer.IComposer;
import com.flipkart.hydra.expression.exception.ExpressionParseException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.Callable;

public class Call implements ICall {

    private final Class<Callable> callableClass;
    private final IComposer composer;

    public Call(Class<Callable> callableClass, IComposer composer) {
        this.callableClass = callableClass;
        this.composer = composer;
    }

    public Call(Class<Callable> callableClass, Object context) throws ExpressionParseException {
        this(callableClass, context, false);
    }

    public Call(Class<Callable> callableClass, Object context, boolean isAlreadyParsed) throws ExpressionParseException {
        this.callableClass = callableClass;
        this.composer = new Composer(context, isAlreadyParsed);
    }

    @Override
    public IComposer getComposer() {
        return composer;
    }

    @Override
    public Callable<Object> getCallable(Object values) throws BadCallableException {
        try {
            Constructor<Callable> constructor = callableClass.getConstructor(Object.class);
            return (Callable<Object>) constructor.newInstance(values);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new BadCallableException("Unable to execute callable", e);
        }
    }

    @Override
    public List<String> getDependencies() {
        return composer.getDependencies();
    }
}
