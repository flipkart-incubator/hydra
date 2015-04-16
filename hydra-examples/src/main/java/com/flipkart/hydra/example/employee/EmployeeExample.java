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

package com.flipkart.hydra.example.employee;

import com.flipkart.hydra.composer.Composer;
import com.flipkart.hydra.composer.DefaultComposer;
import com.flipkart.hydra.composer.exception.ComposerEvaluationException;
import com.flipkart.hydra.composer.exception.ComposerInstantiationException;
import com.flipkart.hydra.dispatcher.DefaultDispatcher;
import com.flipkart.hydra.dispatcher.Dispatcher;
import com.flipkart.hydra.dispatcher.exception.DispatchFailedException;
import com.flipkart.hydra.example.employee.callables.*;
import com.flipkart.hydra.expression.DefaultExpression;
import com.flipkart.hydra.expression.Expression;
import com.flipkart.hydra.expression.exception.ExpressionParseException;
import com.flipkart.hydra.task.DefaultMultiTask;
import com.flipkart.hydra.task.DefaultTask;
import com.flipkart.hydra.task.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmployeeExample {

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public static void main(String[] args) throws ComposerInstantiationException, ExpressionParseException, DispatchFailedException, ComposerEvaluationException {
        Map<String, Object> initialParams = getInitialParams();
        Map<String, Task> tasks = getTasks();
        Composer response = getResponseComposer();

        // Passing Optional Parameter (ExecutorService) so we can use the same for MultiTask
        Dispatcher dispatcher = new DefaultDispatcher(executor);
        Object output = dispatcher.execute(initialParams, tasks, response);

        System.out.println(output);
        dispatcher.shutdown();
    }

    private static Map<String, Object> getInitialParams() {
        Map<String, Object> initialParams = new HashMap<>();
        initialParams.put("employeeName", "John Doe");
        return initialParams;
    }

    private static Map<String, Task> getTasks() throws ComposerInstantiationException, ExpressionParseException {
        Map<String, Task> tasks = new HashMap<>();

        Expression expression1 = new DefaultExpression("$employeeName");
        Composer composer1 = new DefaultComposer(expression1, true);
        Task employeeIDTask = new DefaultTask(EmployeeIdentificationService.class, composer1);

        // Short notation for creating expression on the fly
        Composer composer2 = new DefaultComposer("{{$employeeName}}");
        Task joiningDateTask = new DefaultTask(EmployeeInfoService.class, composer2);

        // Short notation for creating composer on the fly
        Task departmentTask = new DefaultTask(EmployeeDepartmentService.class, "{{$employeeID}}");

        Task salaryTask = new DefaultTask(EmployeeSalaryService.class, "{{$employeeID}}");

        Task locationTask = new DefaultTask(EmployeeLocationService.class, "{{$employeeName}}");

        // MultiTask for executing callable once for each value of the provided looping composer
        // Composer can use $__key and $__value while iterating
        // We also need to provide a ExecutorService to MultiTask
        Task latLngTask = new DefaultMultiTask(executor, LatLngService.class, "{{$location}}", "{{$__value}}");

        tasks.put("joiningDate", joiningDateTask);
        tasks.put("salary", salaryTask);
        tasks.put("department", departmentTask);
        tasks.put("employeeID", employeeIDTask);
        tasks.put("location", locationTask);
        tasks.put("latlng", latLngTask);

        return tasks;
    }

    private static Composer getResponseComposer() throws ComposerInstantiationException {
        Map<String, Object> responseContext = new HashMap<>();
        responseContext.put("employeeName", "{{$employeeName}}");
        responseContext.put("employeeID", "{{$employeeID}}");
        responseContext.put("department", "{{$department}}");

        // Optional data - will not fail on null value
        responseContext.put("salary", "{{#$salary}}");

        // Using expressions to extract part of data
        responseContext.put("city", "{{$location.city}}");

        // Using provided data access functions
        responseContext.put("address", "{{$(join, $(values, $location))}}");

        responseContext.put("latlng", "{{$latlng}}");

        // This recursively iterates over the responseContext and parses any expression that it finds.
        return new DefaultComposer(responseContext);
    }
}
