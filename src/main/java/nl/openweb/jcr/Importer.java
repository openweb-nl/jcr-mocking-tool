package nl.openweb.jcr;

import javax.jcr.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;

import org.apache.commons.beanutils.ConvertUtilsBean;

import nl.openweb.jcr.domain.NodeBean;
import nl.openweb.jcr.json.JsonUtils;

/**
 * Created by Ebrahim on 5/20/2017.
 */
public class Importer {

    public static final String JCR_PRIMARY_TYPE = "jcr:primaryType";
    public static final String JCR_MIXIN_TYPES = "jcr:mixinTypes";

    private final boolean addMixins;
    private final boolean addUuid;
    private final Supplier<Node> rootNodeSupplier;

    public Node createNodesFromJson(String json) throws IOException, RepositoryException {
        NodeBean nodeBean = JsonUtils.parseJson(json);
        return createNodeFromNodeBean(nodeBean);
    }

    public Node createNodesFromJson(InputStream inputStream) throws IOException, RepositoryException {
        NodeBean nodeBean = JsonUtils.parseJson(inputStream);
        return createNodeFromNodeBean(nodeBean);
    }

    private Node createNodeFromNodeBean(NodeBean nodeBean) throws RepositoryException {
        Node node = rootNodeSupplier.get();
        Map<String, Object> map = NodeBeanUtils.nodeBeanToMap(nodeBean);
        updateNode(node, map);
        return node;
    }

    private void updateNode(Node node, Map<String, Object> map) throws RepositoryException {
        ValueFactory valueFactory = node.getSession().getValueFactory();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (NodeBeanUtils.isProperty(entry)) {
                addProperty(node, valueFactory, entry);
            } else {
                addSubnode(node, entry);
            }
        }
    }

    private void addSubnode(Node node, Map.Entry<String, Object> entry) throws RepositoryException {
        Node subNode;
        Map subNodeMap = (Map) entry.getValue();
        if (subNodeMap.containsKey(JCR_PRIMARY_TYPE)) {
            subNode = node.addNode(entry.getKey(), subNodeMap.get(JCR_PRIMARY_TYPE).toString());
        } else {
            subNode = node.addNode(entry.getKey());
        }
        updateNode(subNode, subNodeMap);
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
            node.setProperty(name, jcrValues.toArray(new Value[jcrValues.size()]));
        } else {
            Value jcrValue = toJcrValue(valueFactory, entry.getValue());
            if (JCR_MIXIN_TYPES.equals(name) && addMixins) {
                node.addMixin(jcrValue.getString());
                node.setProperty(name, new Value[]{jcrValue});
            } if ("jcr:uuid".equals(name) && addUuid) {
                setUuid(node, jcrValue.getString());
            } else {
                node.setProperty(name, jcrValue);
            }
        }


    }

    private void setUuid(Node node, String uuid) {
        try {
            Method setter = node.getClass().getMethod("setIdentifier", String.class);
            setter.invoke(node, uuid);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
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
    }

    public static class Builder {
        private boolean addMixins = true;
        private boolean addUuid = false;
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

        public Importer build() {
            return new Importer(this);
        }
    }

}
