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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.stream.Collectors;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;

public abstract class AbstractImporterTest {

    protected Node rootNode;

    @Before
    public abstract void init() throws Exception;

    @After
    public void teardown() throws Exception {
        shutdown();
    }

    protected String getImportPath() {
        return "";
    }

    protected abstract void shutdown() throws Exception;

    protected abstract Importer createImporter() throws Exception;

    @Test
    public void propertyTest() throws RepositoryException {
        propertyBasicValidationForSingleValue(getImportPath() != null && !getImportPath().isEmpty()? rootNode.getNode(getImportPath()) : rootNode, "rootNodeProperty", PropertyType.STRING, "rootNodePropertyValue", Value::getString);
        Node subnode = rootNode.getNode(getImportPath() + "subnode");
        propertyBasicValidationForSingleValue(subnode, "parentNodeProperty", PropertyType.STRING, "parentNodePropertyValue", Value::getString);
        Node subsubnode = rootNode.getNode(getImportPath() + "subnode/subsubnode");
        propertyBasicValidationForSingleValue(subsubnode, "singleValueString", PropertyType.STRING, "stringValue", Value::getString);
        propertyBasicValidationForMultiValue(subsubnode, "multivalueString", PropertyType.STRING, new Object[]{"stringValue01", "stringValue02", "stringValue03"}, Value::getString);
        propertyBasicValidationForSingleValue(subsubnode, "singleValueBoolean", PropertyType.BOOLEAN, true, Value::getBoolean);
        propertyBasicValidationForMultiValue(subsubnode, "multivalueBoolean", PropertyType.BOOLEAN, new Object[]{true, false, true}, Value::getBoolean);
        propertyBasicValidationForSingleValue(subsubnode, "singleValueLong", PropertyType.LONG, 20L, Value::getLong);
        propertyBasicValidationForMultiValue(subsubnode, "multivalueLong", PropertyType.LONG, new Object[]{2L, 6L, 232L}, Value::getLong);
        propertyBasicValidationForSingleValue(subsubnode, "singleValueDouble", PropertyType.DOUBLE, 20.0, Value::getDouble);
        propertyBasicValidationForMultiValue(subsubnode, "multivalueDouble", PropertyType.DOUBLE, new Object[]{2.0, 6.0, 232.0}, Value::getDouble);
        propertyBasicValidationForSingleValue(subsubnode, "singlevalueDate", PropertyType.DATE, getCalendar(1478163995101L), Value::getDate);
        propertyBasicValidationForMultiValue(subsubnode, "multivalueDate", PropertyType.DATE, new Object[]{getCalendar(1478163995101L), getCalendar(1495265315101L)}, Value::getDate);

    }

    @Test
    public void sameNameSiblingsTest() throws RepositoryException {
        Node sameNameSibling1 = rootNode.getNode(getImportPath() + "subnode/sameNameSiblings");
        Assert.assertNotNull(sameNameSibling1);
        propertyBasicValidationForSingleValue(sameNameSibling1, "singleValueString", PropertyType.STRING, "stringValue1", Value::getString);
        Node sameNameSibling2 = rootNode.getNode(getImportPath() + "subnode/sameNameSiblings[2]");
        Assert.assertNotNull(sameNameSibling2);
        propertyBasicValidationForSingleValue(sameNameSibling2, "singleValueString", PropertyType.STRING, "stringValue2", Value::getString);
    }

    @Test
    public void identifierTest() throws RepositoryException {
        assertEquals("cafebabe-cafe-babe-cafe-babecafebabe", rootNode.getIdentifier());
    }

    @Test
    public void mixinTest() throws RepositoryException {
        Node node = rootNode.getNode(getImportPath() + "subnode/subsubnode");
        Property property = node.getProperty("jcr:mixinTypes");
        for (Value v : property.getValues()) {
            MatcherAssert.assertThat("Unexpected mixin", v.getString(),
                    Matchers.is(Matchers.oneOf("mix:test", "mix:referenceable")));
        }
        assertTrue(node.isNodeType("mix:test"));
        assertNotNull(node.getMixinNodeTypes());

        assertTrue(rootNode.getNode(getImportPath() + "subnode/subsubnode").isNodeType("mix:referenceable"));
    }

    @Test
    public void nodeTypeTest() throws RepositoryException {
        Assert.assertTrue(rootNode.getNode(getImportPath() + "subnode").isNodeType("jmt:folder"));
        Assert.assertTrue(rootNode.getNode(getImportPath() + "subnode/subsubnode").isNodeType("jmt:item"));

    }

    protected Calendar getCalendar(long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(date));
        return calendar;
    }

    protected void propertyBasicValidationForSingleValue(Node node, String propertyName, int propertyType, Object expectedValue, ExtractValue extractor) throws RepositoryException {
        Property property = node.getProperty(propertyName);
        assertFalse(property.isMultiple());
        assertEquals(propertyType, property.getType());
        Value value = property.getValue();
        Object actualValue = extractor.apply(value);
        if (propertyType == PropertyType.DATE) {
            assertEquals(((Calendar) expectedValue).getTime().getTime(), ((Calendar) actualValue).getTime().getTime());
        } else {
            assertEquals(expectedValue, actualValue);
        }
    }

    protected void propertyBasicValidationForMultiValue(Node node, String propertyName, int propertyType, Object[] expectedValues, ExtractValue extractor) throws RepositoryException {
        Property property = node.getProperty(propertyName);
        assertTrue(property.isMultiple());
        assertEquals(propertyType, property.getType());
        Value[] values = property.getValues();
        for (int i = 0; i < expectedValues.length; i++) {
            if (propertyType == PropertyType.DATE) {
                assertEquals(((Calendar) expectedValues[i]).getTime().getTime(), ((Calendar) extractor.apply(values[i])).getTime().getTime());
            } else {
                assertEquals(expectedValues[i], extractor.apply(values[i]));
            }
        }
    }

    protected String loadFileAsString(String file) throws URISyntaxException, IOException {
        URI uri = getClass().getClassLoader().getResource(file).toURI();
        return Files.readAllLines(Paths.get(uri)).stream().collect(Collectors.joining("\n"));
    }

    interface ExtractValue {
        Object apply(Value value) throws RepositoryException;
    }
}
