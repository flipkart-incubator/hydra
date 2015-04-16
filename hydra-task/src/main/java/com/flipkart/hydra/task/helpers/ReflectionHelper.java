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

package com.flipkart.hydra.task.helpers;

import java.lang.reflect.Constructor;
import java.util.concurrent.Callable;

public class ReflectionHelper {

    public static Constructor<? extends Callable> getFirstSingleArgConstructor(Class<? extends Callable> callableClass) throws NoSuchMethodException {
        Constructor<?>[] declaredConstructors = callableClass.getDeclaredConstructors();
        for (Constructor constructor : declaredConstructors) {
            Class[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 1) {
                return constructor;
            }
        }

        throw new NoSuchMethodException("Unable to find a single argument constructor.");
    }
}
