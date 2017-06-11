/*
 * Copyright 2017 Open Web IT B.V. (https://www.openweb.nl/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.openweb.jcr;

import java.util.*;

import org.apache.commons.beanutils.ConvertUtilsBean;

import nl.openweb.jcr.domain.NodeBean;
import nl.openweb.jcr.domain.PropertyBean;

public class NodeBeanUtils {

    private static final Set<String> NATIVE_TYPES;

    private static final String VALUE = "value";
    private static final String PRIMITIVE_TYPE = "primitiveType";

    private static final String LONG = "Long";
    private static final String DOUBLE = "Double";
    private static final String BOOLEAN = "Boolean";
    private static final String STRING = "String";

    static {
        HashSet<String> nonNativeTypes = new HashSet<>();
        nonNativeTypes.add(LONG);
        nonNativeTypes.add(DOUBLE);
        nonNativeTypes.add(BOOLEAN);
        nonNativeTypes.add(STRING);
        NATIVE_TYPES = Collections.unmodifiableSet(nonNativeTypes);
    }

    private NodeBeanUtils() {
        // to prevent instantiation of NodeBeanUtils objects.
    }

    public static Map<String, Object> nodeBeanToMap(NodeBean node) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Object> items = node.getNodeOrProperty();
        for (Object item : items) {
            if (item instanceof PropertyBean) {
                PropertyBean property = (PropertyBean) item;
                result.put((property).getName(), getValue(property));
            } else if (item instanceof NodeBean) {
                addSubnode(result, (NodeBean) item);
            }
        }
        return result;
    }

    private static void addSubnode(Map<String, Object> result, NodeBean item) {
        String key = item.getName();
        Map<String, Object> subnode = nodeBeanToMap(item);
        if (result.containsKey(key)) {
            Object o = result.get(key);
            if (o instanceof List) {
                ((List) o).add(subnode);
            } else {
                List<Object> members = new ArrayList<>();
                members.add(o);
                members.add(subnode);
                result.put(key, members);
            }

        } else {
            result.put(key, subnode);
        }
    }

    public static boolean isProperty(Map.Entry<String, Object> entry) {
        boolean result;
        Object value = entry.getValue();
        if (value instanceof Collection) {
            result = ((Collection<Object>) value).stream().findFirst().map(NodeBeanUtils::isItemProperty).orElse(true);
        } else {
            result = isItemProperty(value);
        }
        return result;
    }

    private static boolean isItemProperty(Object value) {
        return !(value instanceof Map) || ((Map) value).containsKey(PRIMITIVE_TYPE);
    }

    public static String getValueType(Object value) {
        String result = STRING;
        if (value instanceof Long || value instanceof Integer) {
            result = LONG;
        } else if (value instanceof Double || value instanceof Float) {
            result = DOUBLE;
        } else if (value instanceof Boolean) {
            result = BOOLEAN;
        } else if (isPrimitiveTypeMap(value)) {
            result = (String) ((Map) value).get(PRIMITIVE_TYPE);
        }
        return result;
    }

    public static String getValueAsString(Object value) {
        String result;
        if (NodeBeanUtils.isPrimitiveTypeMap(value)) {
            result = (String) ((Map) value).get(VALUE);
        } else {
            result = value.toString();
        }
        return result;
    }

    private static boolean isPrimitiveTypeMap(Object value) {
        return value instanceof Map && ((Map) value).containsKey(PRIMITIVE_TYPE);
    }


    private static Object getValue(PropertyBean property) {
        Object result = null;
        if (property != null && property.getValue() != null && !property.getValue().isEmpty()) {

            if (property.isMultiple() != null && property.isMultiple()) {
                List<Object> list = new ArrayList<>();
                for (String v : property.getValue()) {
                    list.add(convertValue(v, property.getType()));
                }
                result = list;
            } else {
                result = convertValue(property.getValue().get(0), property.getType());
            }
        }
        return result;
    }


    private static Object convertValue(String value, String type) {
        Object result;
        if (!NATIVE_TYPES.contains(type)) {
            Map<String, Object> map = new HashMap<>();
            map.put(PRIMITIVE_TYPE, type);
            map.put(VALUE, value);
            result = map;
        } else {
            ConvertUtilsBean convertUtilsBean = new ConvertUtilsBean();
            result = convertUtilsBean.convert(value, getType(type));
        }
        return result;

    }

    private static Class<?> getType(String type) {
        Class<?> result;
        switch (type) {
            case LONG:
                result = Long.class;
                break;
            case DOUBLE:
                result = Double.class;
                break;
            case BOOLEAN:
                result = Boolean.class;
                break;
            default:
                result = String.class;
                break;
        }
        return result;
    }

}
