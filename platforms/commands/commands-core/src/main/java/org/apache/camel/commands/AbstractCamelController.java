/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.camel.catalog.CamelComponentCatalog;
import org.apache.camel.catalog.DefaultCamelComponentCatalog;
import org.apache.camel.commands.internal.RegexUtil;
import org.apache.camel.util.JsonSchemaHelper;

/**
 * Abstract {@link org.apache.camel.commands.CamelController} that implementators should extend.
 */
public abstract class AbstractCamelController implements CamelController {

    private CamelComponentCatalog catalog = new DefaultCamelComponentCatalog();

    @Override
    public List<Map<String, String>> listComponentsCatalog(String filter) throws Exception {
        List<Map<String, String>> answer = new ArrayList<Map<String, String>>();

        if (filter != null) {
            filter = RegexUtil.wildcardAsRegex(filter);
        }

        List<String> names = filter != null ? catalog.findComponentNames(filter) : catalog.findComponentNames();
        for (String name : names) {
            // load component json data, and parse it to gather the component meta-data
            String json = catalog.componentJSonSchema(name);
            List<Map<String, String>> rows = JsonSchemaHelper.parseJsonSchema("component", json, false);

            String description = null;
            String label = null;
            // the status can be:
            // - loaded = in use
            // - classpath = on the classpath
            // - release = available from the Apache Camel release
            String status = "release";
            String type = null;
            String groupId = null;
            String artifactId = null;
            String version = null;
            for (Map<String, String> row : rows) {
                if (row.containsKey("description")) {
                    description = row.get("description");
                } else if (row.containsKey("label")) {
                    label = row.get("label");
                } else if (row.containsKey("javaType")) {
                    type = row.get("javaType");
                } else if (row.containsKey("groupId")) {
                    groupId = row.get("groupId");
                } else if (row.containsKey("artifactId")) {
                    artifactId = row.get("artifactId");
                } else if (row.containsKey("version")) {
                    version = row.get("version");
                }
            }

            Map<String, String> row = new HashMap<String, String>();
            row.put("name", name);
            row.put("status", status);
            if (description != null) {
                row.put("description", description);
            }
            if (label != null) {
                row.put("label", label);
            }
            if (type != null) {
                row.put("type", type);
            }
            if (groupId != null) {
                row.put("groupId", groupId);
            }
            if (artifactId != null) {
                row.put("artifactId", artifactId);
            }
            if (version != null) {
                row.put("version", version);
            }

            answer.add(row);
        }

        return answer;
    }

    @Override
    public Map<String, Set<String>> listLabelCatalog() throws Exception {
        Map<String, Set<String>> answer = new LinkedHashMap<String, Set<String>>();

        Set<String> labels = catalog.findLabels();
        for (String label : labels) {
            List<Map<String, String>> components = listComponentsCatalog(label);
            if (!components.isEmpty()) {
                Set<String> names = new LinkedHashSet<String>();
                for (Map<String, String> info : components) {
                    String name = info.get("name");
                    if (name != null) {
                        names.add(name);
                    }
                }
                answer.put(label, names);
            }
        }

        return answer;
    }

}
