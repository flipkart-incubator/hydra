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

package com.flipkart.hydra.example.random;

import com.flipkart.hydra.composer.Composer;
import com.flipkart.hydra.composer.exception.ComposerEvaluationException;
import com.flipkart.hydra.composer.exception.ComposerInstantiationException;
import com.flipkart.hydra.dispatcher.DefaultDispatcher;
import com.flipkart.hydra.dispatcher.exception.DispatchFailedException;
import com.flipkart.hydra.example.random.callables.EchoValueCallable;
import com.flipkart.hydra.example.random.callables.NullValueCallable;
import com.flipkart.hydra.task.DefaultTask;
import com.flipkart.hydra.task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Explanation -
 *
 * Dispatcher receives 2 parameters as initial input (initialParam1, initialParam2)
 * Its required to make 5 calls (in order of dependencies) and store their output in mentioned keys (taskParam1, taskParam2, taskParam4, taskParam4, taskParam5)
 * Its then required to realize the response context by passing all the required parameters.
 *
 * Dry Run -
 *   + Initial Parameters -
 *      + initialParam1 = initialParamValue1
 *      + initialParam2 = initialParamValue2
 *
 *   + Order of dispatch of tasks -
 *      + taskParam1 - no dependency
 *      + taskParam3 - no dependency
 *      + taskParam5 - no dependency
 *      + taskParam2 - dependent on taskParam1
 *      + taskParam4 - dependent on taskParam1, taskParam2 and taskParam3 (optionally)
 *
 *   + Task Outputs
 *      + taskParam1 = initialParamValue1
 *      + taskParam2 = {value1: initialParamValue1, value2: initialParamValue2, value3: {subValue1: fixedValue}}
 *      + taskParam3 = null
 *      + taskParam4 = {value1: initialParamValue1, value2: {subValue1: null, subValue1: fixedValue}}
 *      + taskParam5 = hardcodedValue
 *
 *   + Final Output
 *      {v1: initialParamValue1, v2: hardcodedValue, v3: {value1: initialParamValue1, value2: {subValue1: null, subValue1: fixedValue}}, v4: [value2, value1, value3]}
 *
 * Illustrations -
 *   + Second and third tasks show the usage of output from previous tasks (and hence the dependency resolution)
 *   + Fourth task shows the usage of optional parameters (taskParam3)
 *   + Fifth task shows the usage of already created composer
 *   + Final Output shows the usage of functions
 */

public class Example {

    public static void main(String[] args) throws ComposerInstantiationException, DispatchFailedException, ComposerEvaluationException {
        Map<String, Object> initialParams = getInitialParams();
        Map<String, Task> tasks = getTasks();
        Map<String, Object> responseContext = new HashMap<String, Object>() {{
            put("v1", "{{$taskParam1}}");
            put("v2", "{{$taskParam5}}");
            put("v3", "{{$taskParam4}}");
            put("v4", "{{$(keys, $taskParam2)}}");
        }};

        ExecutorService executor = Executors.newCachedThreadPool();
        DefaultDispatcher defaultDispatcher = new DefaultDispatcher(executor);

        Object response = defaultDispatcher.execute(initialParams, tasks, responseContext);
        System.out.println(response);

        executor.shutdown();
    }

    private static HashMap<String, Object> getInitialParams() {
        return new HashMap<String, Object>() {{
            put("initialParam1", "initialParamValue1");
            put("initialParam2", "initialParamValue2");
        }};
    }

    private static Map<String, Task> getTasks() throws ComposerInstantiationException {
        return new HashMap<String, Task>() {{
            put("taskParam1", getFirstTask());
            put("taskParam2", getSecondTask());
            put("taskParam3", getThirdTask());
            put("taskParam4", getFourthTask());
            put("taskParam5", getFifthTask());
        }};
    }

    private static Task getFirstTask() throws ComposerInstantiationException {
        HashMap<String, Object> context = new HashMap<String, Object>() {{
            put("value", "{{$initialParam1}}");
        }};

        return new DefaultTask(EchoValueCallable.class, context);
    }

    private static Task getSecondTask() throws ComposerInstantiationException {
        HashMap<String, Object> context = new HashMap<String, Object>() {{
            put("value", new HashMap<String, Object>() {{
                put("value1", "{{$taskParam1}}");
                put("value2", "{{$initialParam2}}");
                put("value3", new HashMap<String, Object>() {{
                    put("subValue1", "fixedValue");
                }});
            }});
        }};

        return new DefaultTask(EchoValueCallable.class, context);
    }

    private static Task getThirdTask() throws ComposerInstantiationException {
        return new DefaultTask(NullValueCallable.class, new HashMap<>());
    }

    private static Task getFourthTask() throws ComposerInstantiationException {
        HashMap<String, Object> context = new HashMap<String, Object>() {{
            put("value", new HashMap<String, Object>() {{
                put("value1", "{{$taskParam1}}");
                put("value2", new HashMap<String, Object>() {{
                    put("subValue1", "{{#$taskParam3.value1}}");
                    put("subValue2", "{{$taskParam2.value3.subValue1}}");
                }});
            }});
        }};

        return new DefaultTask(EchoValueCallable.class, context);
    }

    private static Task getFifthTask() throws ComposerInstantiationException {
        Composer composer = new Composer() {
            @Override
            public Object compose(Map<String, Object> values) throws ComposerEvaluationException {
                return new HashMap<String, Object>() {{
                    put("value", "hardcodedValue");
                }};
            }

            @Override
            public List<String> getDependencies() {
                return new ArrayList<>();
            }
        };

        return new DefaultTask(EchoValueCallable.class, composer);
    }
}
