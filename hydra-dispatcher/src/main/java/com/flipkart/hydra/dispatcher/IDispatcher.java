package com.flipkart.hydra.dispatcher;

import com.flipkart.hydra.call.ICall;
import com.flipkart.hydra.composer.IComposer;
import com.flipkart.hydra.dispatcher.exception.DispatchFailedException;
import com.flipkart.hydra.expression.exception.ExpressionEvaluationException;

import java.util.Map;

public interface IDispatcher {

    public Object execute(Map<String, Object> params, Map<String, ICall> calls, IComposer composer) throws DispatchFailedException, ExpressionEvaluationException;
}
