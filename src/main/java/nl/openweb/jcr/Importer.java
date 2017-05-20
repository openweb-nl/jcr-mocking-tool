package nl.openweb.jcr;

import javax.jcr.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Supplier;

import nl.openweb.jcr.domain.NodeBean;
import nl.openweb.jcr.json.JsonUtils;

/**
 * Created by Ebrahim on 5/20/2017.
 */
public class Importer {

    public static final String JCR_PRIMARY_TYPE = "jcr:primaryType";
    private boolean addMixins = true;
    private Supplier<Node> rootNodeSupplier;

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
                String name = entry.getKey();
                String value = NodeBeanUtils.getValueAsString(entry.getValue());
                if ("jcr:mixinTypes".equals(name) && addMixins) {
                    node.addMixin(value);
                }

                String valueType = NodeBeanUtils.getValueType(entry.getValue());
                int type = PropertyType.valueFromName(valueType);
                Value v = valueFactory.createValue(value, type);
                node.setProperty(name, v);
            } else {
                Node subNode;
                Map subNodeMap = (Map) entry.getValue();
                if (subNodeMap.containsKey(JCR_PRIMARY_TYPE)) {
                    subNode = node.addNode(node.getName(), subNodeMap.get(JCR_PRIMARY_TYPE).toString());
                } else {
                    subNode = node.addNode(node.getName());
                }
                updateNode(subNode, subNodeMap);
            }
        }
    }

    private Importer(Builder builder) {
        this.addMixins = builder.addMixins;
        this.rootNodeSupplier = builder.rootNodeSupplier;
    }

    public static class Builder {
        private boolean addMixins = true;
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

        public Importer build() {
            return new Importer(this);
        }
    }

}
