package nl.openweb.jcr;

import java.util.*;

import org.apache.commons.beanutils.ConvertUtilsBean;

import nl.openweb.jcr.domain.NodeBean;
import nl.openweb.jcr.domain.PropertyBean;

public class NodeBeanUtils {

    private static final Set<String> NATIVE_TYPES;

    public static final String VALUE = "value";
    public static final String PRIMARY_TYPE = "primaryType";

    static {
        HashSet<String> nonNativeTypes = new HashSet<>();
        nonNativeTypes.add("Long");
        nonNativeTypes.add("Double");
        nonNativeTypes.add("Boolean");
        nonNativeTypes.add("String");
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
        return !(value instanceof Map) || ((Map) value).containsKey(PRIMARY_TYPE);
    }

    public static String getValueType(Object value) {
        String result = "String";
        if (value instanceof Long || value instanceof Integer) {
            result = "Long";
        } else if (value instanceof Double || value instanceof Float) {
            result = "Double";
        } else if (value instanceof Boolean) {
            result = "Boolean";
        } else if (isPrimitiveTypeMap(value)) {
            result = (String) ((Map) value).get(PRIMARY_TYPE);
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
        return value instanceof Map && ((Map) value).containsKey(PRIMARY_TYPE);
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
        Object result = null;
        if (!NATIVE_TYPES.contains(type)) {
            Map<String, Object> map = new HashMap<>();
            map.put(PRIMARY_TYPE, type);
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
            case "Long":
                result = Long.class;
                break;
            case "Double":
                result = Double.class;
                break;
            case "Boolean":
                result = Boolean.class;
                break;
            default:
                result = String.class;
                break;
        }
        return result;
    }

}
