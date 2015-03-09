# Hydra
Hydra is a JVM based DispatcherComposer Engine.

Basically, you define the tasks to be performed and the input required for each of those tasks. From then on Hydra takes over - It creates the dependency graph, performs tasks in required order and composes the final output.

For each task to be performed, the input can be composed on any of the initial input parameters or output from already completed tasks. Same is the case with final response.

## Glossary

#### `Dispatcher`
`Dispatcher` is the top-level orchestrator. A Hydra `Dispatcher` execution comprises of -

* Already realized, initial map of parameters - `Map<String, Object>`
* An unordered map of `Task`s to be performed - `Map<String, Task>`
* A response curator - `Composer`

> Hydra provides a default implementation of `Dispatcher` (`DefaultDispatcher`) which can be initialized with or without a `Executor`

> `DefaultDispatcher` allows for the use of a `Object` context (which is used to instantiate a `DefaultComposer` on the fly) as the response curator.

#### `Task`
A `Task` is a combination of a `Composer` and a `Callable`

* The `Composer` is the input curator for the `Callable`.
* Output from `call()` of this `Callable` is the response of this `Task`.

> Hydra provides a default implementation of `Task` (`DefaultTask`) which is initialized with -

> * A `Callable` class that is instantiated on the fly. This class needs to have a single argument constructor which is used to instantiate it with a `Composer` output.
> * A `Composer` or a `Object` context (which is used to instantiate a `DefaultComposer` on the fly)


#### `Composer`
A `Composer` composes the given structure (`Map`, `List`, `String` or `Number`) based on previously realized values.

> Hydra provides a default implementation of `Composer` (`DefaultComposer`) which can take a nested collection and can extract out required dependencies and `Expression`s that need to be realized.

> `Expression`s to be parsed are represented by sandwiching a `String` between `{{` and `}}` and are converted to `DefaultExpression`.

> ###### Examples -

> 1. `{{$someVar}}`
> 2. `{{$someMap.someKey}}`

#### `Expression`
An `Expression` is a string representation of a data-path.

> Hydra provides a default implementation of `Expression` (`DefaultExpression`) which represents an expression as `String`.

> Every variable in the expression starts with a `$`.

> An `Expression` can be marked as optional by starting it with `#`. An optional expression means that `null` is a valid output. And an exception faced while realizing this expression will also respond back with `null`.

> ###### Examples:

> 1. `someConstant` or `1.0` or `1` are static expressions with no variables
> 2. `$someVar` is a variable expression that returns the value of `someVar`
> 3. `$someMap.someKey` is a variable expression that returns value of `someKey` from `someMap`
> 3. `$someMap.someKey[$someList[0]]` is a complex expression depending on more than 1 variable
> 4. `#someMap.someKey` is an optional expression and will return `null` if `someMap` is `null` or if `someMap.someKey` is `null` or non-existent.

## Quick Usage

Add following to `<repositories/>` section of pom.xml -
```xml
<repository>
  <id>clojars</id>
  <name>Clojars repository</name>
  <url>https://clojars.org/repo</url>
</repository>
```

Add following to `<dependencies/>` section of your pom.xml -
```xml
<dependency>
  <groupId>com.flipkart.hydra</groupId>
  <artifactId>hydra-dispatcher</artifactId>
  <version>1.1</version>
</dependency>
```

You can now, call `Dispatcher` using following code
```java
// Already resolved list of parameters
Map<String, Object> initialParams = ...;

// Map of tasks to be dispatched (mapped to the keys that will store the task response)
Map<String, Task> tasks = ...;

// Final response that needs to be realized
Map<String, Object> response = ...;

// Dispatcher Call
Dispatcher dispatcher = new DefaultDispatcher();
Object output = dispatcher.execute(initialParams, tasks, response);
```

## Example

### Assumptions

+ We already have an Employee Name.
+ Assuming all employees have unique names, we want to fetch -
    + Employee ID (provided by EmployeeIdentificationService)
    + Employee Joining Date (provided by EmployeeInfoService)
+ Now using Employee ID, we want to fetch -
    + His department (provided by EmployeeDepartmentService)
    + His salary (provided by EmployeeSalaryService)
        + This information might be confidential and hence can throw UnauthorizedException
        + We don't want to fail in this case
+ Now we want to respond back with all this information

### Steps

1. Create the initial set of parameters -
```java
Map<String, Object> initialParams = new HashMap<>();
initialParams.put("employeeName", "John Doe");
```

2. Create a list of `Task`s to be performed -
##### Task 1 - Fetching `employeeID` from `employeeName`
```java
Expression expression1 = new DefaultExpression("{{$employeeName}}");
Composer composer1 = new DefaultComposer(expression1, true);
Task employeeIDTask = new DefaultTask(EmployeeIdentificationService.class, composer1);
```
##### Task 2 - Fetching `joiningDate` from `employeeName`
```java
// Short notation for creating expression on the fly
Composer composer2 = new DefaultComposer("{{$employeeName}}");
Task joiningDateTask = new DefaultTask(EmployeeInfoService.class, composer2);
```
##### Task 3 - Fetching `department` from `employeeID`
```java
// Short notation for creating composer on the fly
Task departmentTask = new DefaultTask(EmployeeDepartmentService.class, "{{$employeeID}}");
```
##### Task 4 - Fetching `salary` from `employeeID`
```java
Task salaryTask = new DefaultTask(EmployeeSalaryService.class, "{{$employeeID}}");
```
Finally collecting all `Task`s in a `Map`
```java
Map<String, Task> tasks = new HashMap<>();
tasks.put("joiningDate", joiningDateTask);
tasks.put("salary", salaryTask);
tasks.put("department", departmentTask);
tasks.put("employeeID", employeeIDTask);
```

3. Create the response curator
```java
Map<String, Object> responseContext = new HashMap<>();
responseContext.put("employeeName", "{{$employeeName}}");
responseContext.put("employeeID", "{{$employeeID}}");
responseContext.put("department", "{{$department}}");
responseContext.put("salary", "{{#$salary}}"); // Optional data - will not fail on null value
```
```java
// This recursively iterates over the responseContext and parses any expression that it finds.
Composer response = new DefaultComposer(responseContext);
```

4. Dispatch
```java
Dispatcher dispatcher = new DefaultDispatcher();
Object output = dispatcher.execute(initialParams, tasks, response);
```

> Code for this example can be seen [here](https://github.com/flipkart-incubator/hydra/blob/master/hydra-examples/src/main/java/com/flipkart/hydra/example/employee/EmployeeExample.java).

## Salient Points
+ Easy to use interfaces
+ Auto creation/resolution of dependencies graph
+ Notation to express variables/expressions

## Changes

The change log can be found [here](https://github.com/flipkart-incubator/hydra/blob/master/CHANGES.md)

## Contribution, Bugs and Feedback

For bugs, questions and discussions please use the [Github Issues](https://github.com/flipkart-incubator/hydra/issues).
Please follow the [contribution guidelines](https://github.com/flipkart-incubator/hydra/blob/master/CONTRIBUTING.md) when submitting pull requests.

## License

Copyright 2015 Flipkart Internet, pvt ltd.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
