/**
 *  Copyright 2005-2015 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package io.fabric8.insight.camel.audit;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.MessageHistory;
import org.apache.camel.NamedNode;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public final class ScriptUtils {

    private static final SimpleDateFormat format;
    private static final ObjectMapper mapper;

    static {
        format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        mapper = new ObjectMapper();
        mapper.getSerializationConfig().with(format);
        mapper.addMixInAnnotations(MessageHistory.class, DefaultMessageHistoryMixin.class);
        mapper.addMixInAnnotations(NamedNode.class, NamedNodeMixin.class);
    }

    public static String toIso(Date d) {
        return format.format(d);
    }

    public static String toJson(Object o) {
        try {
            if (o instanceof Collection) {
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                for (Object c : (Collection) o) {
                    if (sb.length() > 1) {
                        sb.append(",");
                    }
                    sb.append(toJson(c));
                }
                sb.append("]");
                return sb.toString();
            } else if (o instanceof Map) {
                StringBuilder sb = new StringBuilder();
                sb.append("{");
                for (Map.Entry<Object, Object> e : ((Map<Object, Object>) o).entrySet()) {
                    if (sb.length() > 1) {
                        sb.append(",");
                    }
                    sb.append(toJson(e.getKey().toString()));
                    sb.append(":");
                    sb.append(toJson(e.getValue()));
                }
                sb.append("}");
                return sb.toString();
            } else if (o == null) {
                return "null";
            } else if (o instanceof Date) {
                return "\"" + toIso((Date) o) + "\"";
            } else if (o instanceof MessageHistory) {
                return mapper.writeValueAsString(o);
            } else {
                return mapper.writeValueAsString(o.toString());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not serialize " + o, e);
        }
    }

    public static Map parseJson(String str) {
        try {
            return mapper.readValue(str, Map.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not deserialize " + str, e);
        }
    }

    @JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
    public static interface DefaultMessageHistoryMixin {

        @JsonProperty
        String getRouteId();

        @JsonProperty
        long getElapsed();

        @JsonProperty
        NamedNode getNode();
    }

    @JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
    public static interface NamedNodeMixin {

        @JsonProperty
        String getId();

        @JsonProperty
        String getShortName();

        @JsonProperty
        String getLabel();
    }

}
