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

package com.flipkart.hydra.expression.utils;

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Functions {

    public static List keys(Map map) {
        if (map != null) {
            return new ArrayList(map.keySet());
        }

        return new ArrayList();
    }

    public static List values(Map map) {
        if (map != null) {
            return new ArrayList(map.values());
        }

        return new ArrayList();
    }

    public static Map map(List keys, final List labels, final Map... maps) {
        if (labels.size() == maps.length) {
            Map map = new HashMap();
            for (final Object key : keys) {
                map.put(key, new HashMap<Object, Object>() {{
                    for (int i = 0; i < labels.size(); i++) {
                        put(labels.get(i), maps[i].get(key));
                    }
                }});
            }

            return map;
        }

        throw new RuntimeException("Number of labels do not match number of provided maps");
    }

    public static String join(List list) {
        return Joiner.on(", ").join(list);
    }

    public static String join(Object[] list) {
        return Joiner.on(", ").join(list);
    }
}
