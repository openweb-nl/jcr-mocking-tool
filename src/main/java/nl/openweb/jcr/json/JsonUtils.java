package nl.openweb.jcr.json;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import nl.openweb.jcr.NodeBeanUtils;
import nl.openweb.jcr.domain.NodeBean;
import nl.openweb.jcr.domain.PropertyBean;

/**
 * Created by Ebrahim on 5/20/2017.
 */
public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }

    private JsonUtils() {
        //to prevent instantiation of JsonUtils objects.
    }

    public static String toJson(NodeBean node) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(NodeBeanUtils.nodeBeanToMap(node));
    }

    @SuppressWarnings("unchecked")
    public static NodeBean parseJson(InputStream json) throws IOException {
        return mapToNode(OBJECT_MAPPER.readValue(json, Map.class), "");
    }

    @SuppressWarnings("unchecked")
    public static NodeBean parseJson(String json) throws IOException {
        return mapToNode(OBJECT_MAPPER.readValue(json, Map.class), "");
    }

    @SuppressWarnings("unchecked")
    private static NodeBean mapToNode(Map<String, Object> map, String name) {
        NodeBean result = new NodeBean();
        result.setName(name);
        List<Object> items = new ArrayList<>(map.size());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (NodeBeanUtils.isProperty(entry)) {
                PropertyBean property = createProperty(entry);
                items.add(property);
            } else {
                addNodeBean(items, entry);
            }
        }
        result.setNodeOrProperty(items);
        return result;
    }

    private static void addNodeBean(List<Object> items, Map.Entry<String, Object> entry) {
        Object value = entry.getValue();
        if (value instanceof Collection) {
            for (Object o : ((Collection) value)) {
                items.add(mapToNode((Map<String, Object>) o, entry.getKey()));
            }
        } else {
            items.add(mapToNode((Map<String, Object>) value, entry.getKey()));
        }
    }

    @SuppressWarnings("unchecked")
    private static PropertyBean createProperty(Map.Entry<String, Object> entry) {
        PropertyBean property = new PropertyBean();
        property.setName(entry.getKey());
        boolean multiple = entry.getValue() instanceof Collection;
        property.setMultiple(multiple);
        if (multiple) {
            Collection<Object> val = (Collection<Object>) entry.getValue();
            Optional<Object> first = val.stream().findFirst();
            String type = first.map(NodeBeanUtils::getValueType).orElse("String");
            List<String> values = val.stream().map(NodeBeanUtils::getValueAsString).collect(Collectors.toList());
            property.setType(type);
            property.setValue(values);
        } else {
            property.setType(NodeBeanUtils.getValueType(entry.getValue()));
            property.setValue(Collections.singletonList(NodeBeanUtils.getValueAsString(entry.getValue())));
        }
        return property;
    }


}