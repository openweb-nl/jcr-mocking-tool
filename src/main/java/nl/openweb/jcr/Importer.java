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
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import org.apache.commons.beanutils.ConvertUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.openweb.jcr.json.JsonUtils;
import nl.openweb.jcr.utils.NodeTypeUtils;

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

    private static final Logger LOG = LoggerFactory.getLogger(Importer.class);

    private Importer(Builder builder) {
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
    }

    public Node createNodesFromJson(String json) throws IOException, RepositoryException {
        return createNodeFromNodeBean(JsonUtils.parseJsonMap(json));
    }

    public Node createNodesFromJson(InputStream inputStream) throws IOException, RepositoryException {
        return createNodeFromNodeBean(JsonUtils.parseJsonMap(inputStream));
    }

    private Node createNodeFromNodeBean(Map<String, Object> map) {
        try {
            Node node = rootNodeSupplier.get();
            updateNode(node, map);
            return node;
        } catch (Exception e) {
            throw new JcrImporterException(e.getMessage(), e);
        }
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
                String nodeTypeName = map.get(JCR_PRIMARY_TYPE).toString();
                if (addUnknownTypes) {
                    NodeTypeUtils.createNodeType(node.getSession(), nodeTypeName);
                }
                subNode = node.addNode(name, nodeTypeName);
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
            addMultivalueProperty(node, valueFactory, entry, name);
        } else {
            addSingleValueProperty(node, valueFactory, entry, name);
        }


    }

    private void addSingleValueProperty(Node node, ValueFactory valueFactory, Map.Entry<String, Object> entry, String name) throws RepositoryException {
        Value jcrValue = toJcrValue(valueFactory, entry.getValue());
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

    private void addMultivalueProperty(Node node, ValueFactory valueFactory, Map.Entry<String, Object> entry, String name) throws RepositoryException {
        Collection<Object> values = (Collection) entry.getValue();
        List<Value> jcrValues = new ArrayList<>();
        for (Iterator<Object> iterator = values.iterator(); iterator.hasNext(); ) {
            Value jcrValue = toJcrValue(valueFactory, iterator.next());
            if (JCR_MIXIN_TYPES.equals(name) && addMixins) {
                String mixinName = jcrValue.getString();
                if (addUnknownTypes) {
                    NodeTypeUtils.createMixin(node.getSession(), mixinName);
                }
                node.addMixin(mixinName);
            }
            jcrValues.add(jcrValue);
        }
        if (!protectedProperties.contains(name) || setProtectedProperties) {
            node.setProperty(name, jcrValues.toArray(new Value[jcrValues.size()]));
        }
    }

    private void setUuid(Node node, String uuid) {
        try {
            Method setter = node.getClass().getMethod("setIdentifier", String.class);
            setter.invoke(node, uuid);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOG.debug("While trying to set the uuid the following exception was thrown: " + e.getMessage(), e);
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
    interface SupplierWithException<T> {
        T get() throws Exception;
    }

}
