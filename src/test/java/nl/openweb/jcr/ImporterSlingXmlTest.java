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

import nl.openweb.jcr.importer.JcrImporter;
import nl.openweb.jcr.importer.JsonImporter;
import nl.openweb.jcr.importer.XmlImporter;
import org.apache.sling.testing.mock.jcr.MockJcr;

import javax.jcr.*;

public class ImporterSlingXmlTest extends AbstractImporterXmlTest {


    @Override
    protected JcrImporter createImporter(String format) throws RepositoryException {
        Session session = MockJcr.newSession();
        JcrImporter importer;
        if (JsonImporter.FORMAT.equals(format)) {
            importer = new JsonImporter(session.getRootNode());
        } else if (XmlImporter.FORMAT.equals(format)) {
            importer = new XmlImporter(session.getRootNode());
        } else {
            throw new IllegalArgumentException("Unknown format: " + format);
        }

        return importer
                .addMixins(false)
                .addUuid(true)
                .setProtectedProperties(true);

    }

    @Override
    protected void shutdown() {
        // ignore
    }


    @Override
    public void propertyTest() throws RepositoryException {
        // propertyBasicValidationForSingleValue(rootNode, "rootNodeProperty", PropertyType.STRING, "rootNodePropertyValue", Value::getString);
        Node subnode = rootNode.getNode("subnode");
        propertyBasicValidationForSingleValue(subnode, "parentNodeProperty", PropertyType.STRING, "parentNodePropertyValue", Value::getString);
        Node subsubnode = rootNode.getNode("subnode/subsubnode");
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

    @Override
    public void identifierTest() throws RepositoryException {
        // disabling the test
    }

    @Override
    public void sameNameSiblingsTest() throws RepositoryException {
        // disabling the test
    }

    @Override
    public void mixinTest() throws RepositoryException {
        // disabling the test
    }
}
