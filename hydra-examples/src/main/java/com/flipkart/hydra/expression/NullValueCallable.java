package com.flipkart.hydra.expression;

import java.util.Map;
import java.util.concurrent.Callable;

public class NullValueCallable implements Callable {

    private final Map<String, Object> values;

    public NullValueCallable(Map<String, Object> values) {
        this.values = values;
    }

    @Override
    public Object call() throws Exception {
        return null;
    }
}
