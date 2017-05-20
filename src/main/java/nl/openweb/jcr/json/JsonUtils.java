package nl.openweb.jcr.json;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.beanutils.ConvertUtilsBean;

import nl.openweb.jcr.domain.Node;
import nl.openweb.jcr.domain.Property;

/**
 * Created by Ebrahim on 5/20/2017.
 */
public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonUtils() {
        //to prevent instantiation of JsonUtils objects.
    }

    public static String toJson(Node node) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(nodeToMap(node));
    }

    @SuppressWarnings("unchecked")
    public static Node parseJson(String json) throws IOException {
        return mapToNode(OBJECT_MAPPER.readValue(json, Map.class), "");
    }

    private static Node mapToNode(Map<String, Object> map, String name) {
        Node result = new Node();
        result.setName(name);
        List<Object> items = new ArrayList<>(map.size());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (isPropery(entry)) {
                Property property = createProperty(entry);
                items.add(property);
            } else {
                items.add(mapToNode((Map<String, Object>) entry.getValue(), entry.getKey()));
            }
        }
        result.setNodeOrProperty(items);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Property createProperty(Map.Entry<String, Object> entry) {
        Property property = new Property();
        property.setName(entry.getKey());
        boolean multiple = entry.getValue() instanceof Collection;
        property.setMultiple(multiple);
        if (multiple) {
            Collection<Object> val = (Collection<Object>) entry.getValue();
            Optional<Object> first = val.stream().findFirst();
            String type = first.map(JsonUtils::getValueType).orElse("String");
            List<String> values = val.stream().map(JsonUtils::getValueAsString).collect(Collectors.toList());
            property.setType(type);
            property.setValue(values);
        } else {
            property.setType(getValueType(entry.getValue()));
            property.setValue(Collections.singletonList(getValueAsString(entry.getValue())));
        }
        return property;
    }

    private static String getValueAsString(Object value) {
        //FIXME
        return value.toString();
    }

    private static boolean isPropery(Map.Entry<String, Object> entry) {
        return !(entry.getValue() instanceof Map);
    }

    private static Map<String, Object> nodeToMap(Node node) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Object> items = node.getNodeOrProperty();
        for (Object item : items) {
            if (item instanceof Property) {
                Property property = (Property) item;
                result.put((property).getName(), getValue(property));
            } else if (item instanceof Node) {
                result.put(((Node) item).getName(), nodeToMap((Node) item));
            }
        }
        return result;
    }

    private static Object getValue(Property property) {
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
        ConvertUtilsBean convertUtilsBean = new ConvertUtilsBean();
        return convertUtilsBean.convert(value, getType(type));
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

    private static String getValueType(Object value) {
        String result = "String";
        if (value instanceof Long || value instanceof Integer) {
            result = "Long";
        } else if (value instanceof Double || value instanceof Float) {
            result = "Double";
        } else if (value instanceof Boolean) {
            result = "Boolean";
        }
        return result;
    }

}
