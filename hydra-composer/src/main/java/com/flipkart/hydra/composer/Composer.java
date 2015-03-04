package com.flipkart.hydra.composer;

import com.flipkart.hydra.composer.exception.ComposerEvaluationException;

import java.util.List;
import java.util.Map;

public interface Composer {

    public Object compose(Map<String, Object> values) throws ComposerEvaluationException;

    public List<String> getDependencies();
}
