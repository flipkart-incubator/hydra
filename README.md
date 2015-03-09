# Hydra
Hydra is a JVM based DispatcherComposer Engine.

Basically, you define the tasks to be performed and the input required for each of those tasks.
From then on Hydra takes over - It creates the dependency graph, performs tasks in required
order and composes the final output.

For each task to be performed, the input can be composed on any of the initial input parameters
or output from already completed tasks. Same is the case with final response.

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
  <version>1.0</version>
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
Object output = defaultDispatcher.execute(initialParams, tasks, response);
```

## Salient Points
+ Easy to use interfaces
+ Auto creation/resolution of dependencies graph
+ Notation to express variables/expressions

## Changes

The change log can be found [here](https://github.com/flipkart-incubator/hydra/blob/master/CHANGES.md)

## Contribution, Bugs and Feedback

For bugs, questions and discussions please use the [Github Issues](https://github.com/flipkart-incubator/hydra/issues).
Please follow the [contribution guidelines](https://github.com/flipkart-incubator/hydra/blob/master/CONTRIBUTING.md) when submitting pull requests.

##License

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
