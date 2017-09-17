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

import javax.jcr.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;

import org.apache.commons.beanutils.ConvertUtilsBean;

import nl.openweb.jcr.domain.NodeBean;
import nl.openweb.jcr.domain.PropertyBean;
import nl.openweb.jcr.json.JsonUtils;
import nl.openweb.jcr.utils.NodeTypeUtils;
import nl.openweb.jcr.utils.PathUtils;
import nl.openweb.jcr.utils.ReflectionUtils;

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
    private final boolean addUnknownTypes;
    private final SupplierWithException<Node> rootNodeSupplier;
    private JAXBContext jaxbContext;

    private Importer(Builder builder) {
        try {
            this.addMixins = builder.addMixins;
            this.rootNodeSupplier = builder.rootNodeSupplier;
            this.addUuid = builder.addUuid;
            this.setProtectedProperties = builder.setProtectedProperties;
            this.saveSession = builder.saveSession;
            this.addUnknownTypes = builder.addUnknownTypes;
            HashSet<String> set = new HashSet<>();
            set.add(JCR_PRIMARY_TYPE);
            set.add(JCR_MIXIN_TYPES);
            set.add(JCR_UUID);
            this.protectedProperties = Collections.unmodifiableSet(set);
            this.jaxbContext = JAXBContext.newInstance(NodeBean.class, PropertyBean.class);
        } catch (JAXBException e) {
            throw new JcrImporterException(e.getMessage(), e);
        }
    }

    public Node createNodesFromJson(String json) {
        return createNodesFromJson(json, null, null);
    }

    public Node createNodesFromJson(String json, String path) {
        return createNodesFromJson(json, path, null);
    }

    public Node createNodesFromJson(String json, String path, String intermediateNodeType) {
        try {
            return createNodeFromNodeBean(JsonUtils.parseJsonMap(json), path, intermediateNodeType);
        } catch (IOException e) {
            throw new JcrImporterException(e.getMessage(), e);
        }
    }

    public Node createNodesFromJson(InputStream inputStream) {
        return createNodesFromJson(inputStream, null, null);
    }

    public Node createNodesFromJson(InputStream inputStream, String path) {
        return createNodesFromJson(inputStream, path, null);
    }

    public Node createNodesFromJson(InputStream inputStream, String path, String intermediateNodeType) {
        try {
            validate(inputStream);
            return createNodeFromNodeBean(JsonUtils.parseJsonMap(inputStream), path, intermediateNodeType);
        } catch (IOException e) {
            throw new JcrImporterException(e.getMessage(), e);
        }
    }



    public Node createNodesFromXml(String xml) {
        return createNodesFromXml(xml, null);
    }

    public Node createNodesFromXml(String xml, String path) {
        return createNodesFromXml(xml, path, null);
    }

    public Node createNodesFromXml(String xml, String path, String intermediateNodeType) {
        return this.createNodesFromXml(new ByteArrayInputStream(xml.getBytes()), path, intermediateNodeType);
    }

    public Node createNodesFromXml(InputStream inputStream) {
        return createNodesFromXml(inputStream, null, null);
    }

    public Node createNodesFromXml(InputStream inputStream, String path) {
        return createNodesFromXml(inputStream, path, null);
    }

    public Node createNodesFromXml(InputStream inputStream, String path, String intermediateNodeType) {
        try {
            validate(inputStream);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Object unmarshaled = unmarshaller.unmarshal(inputStream);
            if (unmarshaled instanceof NodeBean) {
                return createNodeFromNodeBean(NodeBeanUtils.nodeBeanToMap((NodeBean) unmarshaled), path, intermediateNodeType);
            } else {
                throw new JcrImporterException("The given XML file is not of the right format");
            }
        } catch (JAXBException e) {
            throw new JcrImporterException(e.getMessage(), e);
        }
    }

    private void validate(InputStream inputStream) {
        if (inputStream == null) {
            throw new JcrImporterException("InputSteam may not be null.");
        }
    }


    private Node createNodeFromNodeBean(Map<String, Object> map, String path, String intermediateNodeType) {
        try {
            Node root = rootNodeSupplier.get();
            String nodeType = map.containsKey(JCR_PRIMARY_TYPE) ? getPrimaryType(map) : intermediateNodeType;
            Node node = getOrCreateNode(root, path, intermediateNodeType, nodeType);
            updateNode(node, map);
            return root;
        } catch (Exception e) {
            throw new JcrImporterException(e.getMessage(), e);
        }
    }

    private Node getOrCreateNode(Node rootNode, String path, String intermediateNodeType, String nodeType) {
        Node result = rootNode;
        if (path != null && !path.isEmpty()) {
            String[] nodes = PathUtils.normalizePath(path).split("/");
            for (int i = 0; i < nodes.length; i++) {
                String n = nodes[i];
                result = getOrCreateNode(i + 1 != nodes.length ? intermediateNodeType : nodeType, result, n);
            }
        }
        return result;
    }

    private Node getOrCreateNode(String nodeType, Node node, String n) {
        try {
            Node result;
            if (node.hasNode(n)) {
                result = node.getNode(n);
            } else if (nodeType != null && !nodeType.isEmpty()) {
                if (addUnknownTypes) {
                    NodeTypeUtils.createNodeType(node.getSession(), nodeType);
                }
                result = node.addNode(n, nodeType);
            } else {
                result = node.addNode(n);
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
                subNode = addSubnodeWithPrimaryType(node, name, map);
            } else {
                subNode = addSubnodeWithoutPrimaryType(node, name, map);
            }
            updateNode(subNode, map);
        } else if (obj instanceof Collection) {
            for (Object item : (Collection) obj) {
                addSubnode(node, name, item);
            }
        }
    }

    private Node addSubnodeWithoutPrimaryType(Node node, String name, Map map) throws RepositoryException {
        Node subNode;
        Method method = ReflectionUtils.getMethod(node, "addNodeWithUuid",
                String.class, String.class);
        if (addUuid && map.containsKey(JCR_UUID) && method != null) {
            subNode = (Node) ReflectionUtils.invokeMethod(method, node, name, map.get(JCR_UUID));
        } else {
            subNode = node.addNode(name);
        }
        return subNode;
    }

    private Node addSubnodeWithPrimaryType(Node node, String name, Map map) throws RepositoryException {
        Node subNode;
        String nodeTypeName = getPrimaryType(map);
        if (addUnknownTypes) {
            NodeTypeUtils.createNodeType(node.getSession(), nodeTypeName);
        }
        Method method = ReflectionUtils.getMethod(node, "addNodeWithUuid",
                String.class, String.class, String.class);

        if (addUuid && map.containsKey(JCR_UUID) && method != null) {
            subNode = (Node) ReflectionUtils.invokeMethod(method, node, name, nodeTypeName, map.get(JCR_UUID));
        } else {
            subNode = node.addNode(name, nodeTypeName);
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

    public static class Builder {
        private boolean addMixins = true;
        private boolean addUuid = false;
        private boolean setProtectedProperties = false;
        private boolean saveSession = true;
        private boolean addUnknownTypes = false;
        private final SupplierWithException<Node> rootNodeSupplier;


        public Builder(SupplierWithException<Node> rootNodeSupplier) {
            this.rootNodeSupplier = rootNodeSupplier;
            if (this.rootNodeSupplier == null) {
                throw new IllegalArgumentException("supplier is required.");
            }
        }

        public Builder addMixins(boolean addMixins) {
            this.addMixins = addMixins;
            return this;
        }

        public Builder addUuid(boolean addUuid) {
            this.addUuid = addUuid;
            return this;
        }

        public Builder setProtectedProperties(boolean setProtectedProperties) {
            this.setProtectedProperties = setProtectedProperties;
            return this;
        }

        public Builder saveSession(boolean saveSession) {
            this.saveSession = saveSession;
            return this;
        }

        public Builder addUnknownTypes(boolean addUnknownTypes) {
            this.addUnknownTypes = addUnknownTypes;
            return this;
        }

        public Importer build() {
            return new Importer(this);
        }
    }

    @FunctionalInterface
    public interface SupplierWithException<T> {
        T get() throws Exception;
    }

}
