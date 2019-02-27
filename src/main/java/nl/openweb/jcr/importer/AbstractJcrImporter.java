package nl.openweb.jcr.importer;

import nl.openweb.jcr.JcrImporterException;
import nl.openweb.jcr.NodeBeanUtils;
import nl.openweb.jcr.utils.NodeTypeUtils;
import nl.openweb.jcr.utils.PathUtils;
import nl.openweb.jcr.utils.ReflectionUtils;
import org.apache.commons.beanutils.ConvertUtilsBean;

import javax.jcr.*;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;

import static nl.openweb.jcr.utils.ReflectionUtils.unwrapNodeDecorator;

/**
 * @author Ivor
 */
public abstract class AbstractJcrImporter implements JcrImporter {

    private final Set<String> protectedProperties;
    private boolean addMixins = true;
    private boolean addUuid = false;
    private boolean setProtectedProperties = false;
    private boolean saveSession = true;
    private boolean addUnknownTypes = false;
    private final Node rootNode;

    public AbstractJcrImporter(Node rootNode) {
        this.rootNode = rootNode;
        if (rootNode == null) {
            throw new JcrImporterException("rootNode is not allowed to be null");
        }
        HashSet<String> set = new HashSet<>();
        set.add(JCR_PRIMARY_TYPE);
        set.add(JCR_MIXIN_TYPES);
        set.add(JCR_UUID);
        this.protectedProperties = Collections.unmodifiableSet(set);
    }

    @Override
    public JcrImporter addMixins(boolean addMixins) {
        this.addMixins = addMixins;
        return this;
    }

    public boolean isAddMixins() {
        return addMixins;
    }

    @Override
    public AbstractJcrImporter addUuid(boolean addUuid) {
        this.addUuid = addUuid;
        return this;
    }

    public boolean isAddUuid() {
        return addUuid;
    }

    @Override
    public AbstractJcrImporter setProtectedProperties(boolean setProtectedProperties) {
        this.setProtectedProperties = setProtectedProperties;
        return this;
    }

    public boolean isSetProtectedProperties() {
        return setProtectedProperties;
    }

    @Override
    public AbstractJcrImporter saveSession(boolean saveSession) {
        this.saveSession = saveSession;
        return this;
    }

    public boolean isSaveSession() {
        return saveSession;
    }

    @Override
    public AbstractJcrImporter addUnknownTypes(boolean addUnknownTypes) {
        this.addUnknownTypes = addUnknownTypes;
        return this;
    }

    public boolean isAddUnknownTypes() {
        return addUnknownTypes;
    }

    @Override
    public Node getRootNode() {
        return rootNode;
    }


    public Node createNodes(String source) {
        return createNodes(source, null, null);
    }

    public Node createNodes(String source, String path) {
        return createNodes(source, path, null);
    }

    public Node createNodes(InputStream inputStream) {
        return createNodes(inputStream, null, null);
    }

    public Node createNodes(InputStream inputStream, String path) {
        return createNodes(inputStream, path, null);
    }


    Node createNodeFromNodeBean(Map<String, Object> map, String path, String intermediateNodeType) {
        try {
            Node node = getOrCreateNode(rootNode, path, intermediateNodeType, map);
            updateNode(node, map);
            return rootNode;
        } catch (Exception e) {
            throw new JcrImporterException(e.getMessage(), e);
        }
    }

    private Node getOrCreateNode(Node rootNode, String path, String intermediateNodeType, Map<String, Object> map) {
        Node result = rootNode;
        if (path != null && !path.isEmpty()) {
            String[] nodes = PathUtils.normalizePath(path).split("/");
            for (int i = 0; i < nodes.length; i++) {
                String n = nodes[i];
                String nodeType = i + 1 == nodes.length && map.containsKey(JCR_PRIMARY_TYPE) ? getPrimaryType(map) : intermediateNodeType;
                result = getOrCreateNode(nodeType, result, n, i + 1 == nodes.length ? (String) map.get(JCR_UUID) : null);
            }
        }
        return result;
    }

    private Node getOrCreateNode(String nodeType, Node node, String name, String uuid) {
        try {
            Node result;
            if (node.hasNode(name)) {
                result = node.getNode(name);
            } else if (nodeType != null && !nodeType.isEmpty()) {
                if (addUnknownTypes) {
                    NodeTypeUtils.createNodeType(node.getSession(), nodeType);
                }
                result = addSubnodeWithPrimaryType(node, name, nodeType, uuid);
            } else {
                result = addSubnodeWithoutPrimaryType(node, name, uuid);
            }
            return result;
        } catch (RepositoryException e) {
            throw new JcrImporterException(e.getMessage(), e);
        }
    }

    private void updateNode(Node node, Map<String, Object> map) throws RepositoryException {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (NodeBeanUtils.isProperty(entry)) {
                addProperty(node, entry);
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
            if (map.containsKey(JCR_PRIMARY_TYPE) && !"".equals(map.get(JCR_PRIMARY_TYPE))) {
                subNode = addSubnodeWithPrimaryType(node, name, getPrimaryType(map), (String) map.get(JCR_UUID));
            } else {
                subNode = addSubnodeWithoutPrimaryType(node, name, (String) map.get(JCR_UUID));
            }
            updateNode(subNode, map);
        } else if (obj instanceof Collection) {
            for (Object item : (Collection) obj) {
                addSubnode(node, name, item);
            }
        }
    }

    private Node addSubnodeWithoutPrimaryType(Node node, String name, String uuid) throws RepositoryException {
        Node subNode;
        Node realNode = unwrapNodeDecorator(node);
        Method method = ReflectionUtils.getMethod(realNode, "addNodeWithUuid",
                String.class, String.class);
        if (addUuid && uuid != null && method != null) {
            subNode = (Node) ReflectionUtils.invokeMethod(method, realNode, name, uuid);
        } else {
            subNode = realNode.addNode(name);
        }
        return subNode;
    }

    private Node addSubnodeWithPrimaryType(Node node, String name, String nodeTypeName, String uuid) throws RepositoryException {
        Node subNode;
        if (addUnknownTypes) {
            NodeTypeUtils.createNodeType(node.getSession(), nodeTypeName);
            if (name.contains(":")) {
                NodeTypeUtils.getOrRegisterNamespace(node.getSession(), name);
            }
        }
        Node realNode = unwrapNodeDecorator(node);
        Method method = ReflectionUtils.getMethod(realNode, "addNodeWithUuid",
                String.class, String.class, String.class);

        if (addUuid && uuid != null && method != null) {
            subNode = (Node) ReflectionUtils.invokeMethod(method, realNode, name, nodeTypeName, uuid);
        } else {
            subNode = realNode.addNode(name, nodeTypeName);
        }
        return subNode;
    }

    private String getPrimaryType(Map map) {
        String result;
        Object value = map.get(JCR_PRIMARY_TYPE);
        if (value instanceof Map && ((Map) value).containsKey("value")) {
            result = ((Map) value).get("value").toString();
        } else {
            result = value.toString();
        }
        return result;
    }

    private void addProperty(Node node, Map.Entry<String, Object> entry) throws RepositoryException {
        String name = entry.getKey();
        if (addUnknownTypes) {
            NodeTypeUtils.getOrRegisterNamespace(node.getSession(), name);
        }
        Object value = entry.getValue();
        if (value instanceof Collection) {
            addMultivalueProperty(node, entry, name);
        } else if (NodeBeanUtils.isValueNotNull(value)) {
            addSingleValueProperty(node, entry, name);
        }


    }


    private void addSingleValueProperty(Node node, Map.Entry<String, Object> entry, String name) throws RepositoryException {
        Value jcrValue = toJcrValue(node.getSession(), entry.getValue(), name);
        if ("jcr:uuid".equals(name) && addUuid) {
            setUuid(node, jcrValue.getString());
        }
        if (JCR_MIXIN_TYPES.equals(name) && addMixins) {
            String mixinName = jcrValue.getString();
            if (addUnknownTypes) {
                NodeTypeUtils.createMixin(node.getSession(), mixinName);
            }
            node.addMixin(mixinName);
            if (setProtectedProperties) {
                node.setProperty(name, new Value[]{jcrValue});
            }
        } else if (!protectedProperties.contains(name) || setProtectedProperties) {
            node.setProperty(name, jcrValue);
        }
    }

    private void addMultivalueProperty(Node node, Map.Entry<String, Object> entry, String name) throws RepositoryException {
        Collection<Object> values = (Collection) entry.getValue();
        List<Value> jcrValues = new ArrayList<>();
        for (Iterator<Object> iterator = values.iterator(); iterator.hasNext(); ) {
            Object value = iterator.next();
            if (NodeBeanUtils.isValueNotNull(value)) {
                Value jcrValue = toJcrValue(node.getSession(), value, name);
                addMixinIfRequired(node, name, jcrValue);
                jcrValues.add(jcrValue);
            }
        }
        if (!protectedProperties.contains(name) || setProtectedProperties) {
            node.setProperty(name, jcrValues.toArray(new Value[jcrValues.size()]));
        }
    }

    private void addMixinIfRequired(Node node, String name, Value jcrValue) throws RepositoryException {
        if (JCR_MIXIN_TYPES.equals(name) && addMixins) {
            String mixinName = jcrValue.getString();
            if (addUnknownTypes) {
                NodeTypeUtils.createMixin(node.getSession(), mixinName);
            }
            node.addMixin(mixinName);
        }
    }

    private void setUuid(Node node, String uuid) {
        Method setter = ReflectionUtils.getMethod(node, "setIdentifier", String.class);
        if (setter != null) {
            ReflectionUtils.invokeMethod(setter, node, uuid);
        }

    }

    private Value toJcrValue(Session session, Object value, String propertyName) throws RepositoryException {
        Value result;
        ValueFactory valueFactory = session.getValueFactory();
        String valueType = NodeBeanUtils.getValueType(value);
        ConvertUtilsBean convertUtilsBean = new ConvertUtilsBean();
        int type = PropertyType.valueFromName(valueType);
        String valueAsString = NodeBeanUtils.getValueAsString(value);
        if (addUnknownTypes && type == PropertyType.NAME) {
            if (JCR_MIXIN_TYPES.equals(propertyName)) {
                NodeTypeUtils.createMixin(session, valueAsString);
            } else {
                NodeTypeUtils.createNodeType(session, valueAsString);
            }
        }
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


}
