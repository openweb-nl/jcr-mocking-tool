package nl.openweb.jcr;

import javax.jcr.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;

import org.apache.commons.beanutils.ConvertUtilsBean;

import nl.openweb.jcr.json.JsonUtils;

/**
 * Created by Ebrahim on 5/20/2017.
 */
public class Importer {

    public static final String JCR_PRIMARY_TYPE = "jcr:primaryType";
    public static final String JCR_MIXIN_TYPES = "jcr:mixinTypes";
    public static final String JCR_UUID = "jcr:uuid";
    private final Set<String> protectedProperties;
    private final boolean setProtectedProperties;
    private final boolean saveSession;
    private final boolean addMixins;
    private final boolean addUuid;
    private final Supplier<Node> rootNodeSupplier;

    public Node createNodesFromJson(String json) throws IOException, RepositoryException {
        return createNodeFromNodeBean(JsonUtils.parseJsonMap(json));
    }

    public Node createNodesFromJson(InputStream inputStream) throws IOException, RepositoryException {
        return createNodeFromNodeBean(JsonUtils.parseJsonMap(inputStream));
    }

    private Node createNodeFromNodeBean(Map<String, Object> map) throws RepositoryException {
        Node node = rootNodeSupplier.get();
        updateNode(node, map);
        return node;
    }

    private void updateNode(Node node, Map<String, Object> map) throws RepositoryException {
        ValueFactory valueFactory = node.getSession().getValueFactory();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (NodeBeanUtils.isProperty(entry)) {
                addProperty(node, valueFactory, entry);
            } else {
                addSubnode(node, entry.getKey(), entry.getValue());
            }
        }
        if (saveSession) {
            node.getSession().save();
        }
    }

    private void addSubnode(Node node, String name, Object obj) throws RepositoryException {
        Node subNode;
        if (obj instanceof Map) {
            Map map = (Map) obj;
            if (map.containsKey(JCR_PRIMARY_TYPE)) {
                subNode = node.addNode(name, map.get(JCR_PRIMARY_TYPE).toString());
            } else {
                subNode = node.addNode(name);
            }
            updateNode(subNode, map);
        } else if (obj instanceof Collection) {
            for (Object item : (Collection) obj) {
                addSubnode(node, name, item);
            }
        }
    }

    private void addProperty(Node node, ValueFactory valueFactory, Map.Entry<String, Object> entry) throws RepositoryException {
        String name = entry.getKey();
        if (entry.getValue() instanceof Collection) {
            Collection<Object> values = (Collection) entry.getValue();
            List<Value> jcrValues = new ArrayList<>();
            for (Iterator<Object> iterator = values.iterator(); iterator.hasNext(); ) {

                Value jcrValue = toJcrValue(valueFactory, iterator.next());
                if (JCR_MIXIN_TYPES.equals(name) && addMixins) {
                        node.addMixin(jcrValue.getString());
                }
                jcrValues.add(jcrValue);
            }
            if (!protectedProperties.contains(name) || setProtectedProperties) {
                node.setProperty(name, jcrValues.toArray(new Value[jcrValues.size()]));
            }
        } else {
            Value jcrValue = toJcrValue(valueFactory, entry.getValue());
            if ("jcr:uuid".equals(name) && addUuid) {
                setUuid(node, jcrValue.getString());
            }
            if (JCR_MIXIN_TYPES.equals(name) && addMixins) {
                node.addMixin(jcrValue.getString());
                if (setProtectedProperties) {
                    node.setProperty(name, new Value[]{jcrValue});
                }
            } else if (!protectedProperties.contains(name) || setProtectedProperties) {
                node.setProperty(name, jcrValue);
            }
        }


    }

    private void setUuid(Node node, String uuid) {
        try {
            Method setter = node.getClass().getMethod("setIdentifier", String.class);
            setter.invoke(node, uuid);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // ignore
        }
    }

    private Value toJcrValue(ValueFactory valueFactory, Object value) throws ValueFormatException {
        Value result;
        String valueType = NodeBeanUtils.getValueType(value);
        ConvertUtilsBean convertUtilsBean = new ConvertUtilsBean();
        int type = PropertyType.valueFromName(valueType);
        String valueAsString = NodeBeanUtils.getValueAsString(value);
        switch (type) {
            case PropertyType.BOOLEAN:
                result = valueFactory.createValue((Boolean) convertUtilsBean.convert(valueAsString, Boolean.class));
                break;
            case PropertyType.DOUBLE:
                result = valueFactory.createValue((Double) convertUtilsBean.convert(valueAsString, Double.class));
                break;
            case PropertyType.LONG:
                result = valueFactory.createValue((Long) convertUtilsBean.convert(valueAsString, Long.class));
                break;
            default:
                result = valueFactory.createValue(valueAsString, type);
                break;
        }
        return result;
    }

    private Importer(Builder builder) {
        this.addMixins = builder.addMixins;
        this.rootNodeSupplier = builder.rootNodeSupplier;
        this.addUuid = builder.addUuid;
        this.setProtectedProperties = builder.setProtectedProperties;
        this.saveSession = builder.saveSession;
        HashSet<String> set = new HashSet<>();
        set.add(JCR_PRIMARY_TYPE);
        set.add(JCR_MIXIN_TYPES);
        set.add(JCR_UUID);
        this.protectedProperties = Collections.unmodifiableSet(set);
    }

    public static class Builder {
        private boolean addMixins = true;
        private boolean addUuid = false;
        private boolean setProtectedProperties = false;
        private boolean saveSession = true;
        private final Supplier<Node> rootNodeSupplier;


        public Builder(Supplier<Node> rootNodeSupplier) {
            this.rootNodeSupplier = rootNodeSupplier;
            if (this.rootNodeSupplier == null) {
                throw new IllegalArgumentException("supplier is required.");
            }
        }

        public Builder setAddMixins(boolean addMixins) {
            this.addMixins = addMixins;
            return this;
        }

        public Builder setAddUuid(boolean addUuid) {
            this.addUuid = addUuid;
            return this;
        }

        public Builder setProtectedProperties(boolean setProtectedProperties) {
            this.setProtectedProperties = setProtectedProperties;
            return this;
        }

        public Builder setSaveSession(boolean saveSession) {
            this.saveSession = saveSession;
            return this;
        }

        public Importer build() {
            return new Importer(this);
        }
    }

}
