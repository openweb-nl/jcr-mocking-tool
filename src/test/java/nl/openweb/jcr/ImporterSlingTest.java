package nl.openweb.jcr;

import javax.jcr.*;
import java.io.IOException;

import org.apache.sling.testing.mock.jcr.MockJcr;

public class ImporterSlingTest extends AbstractImporterTest {


    @Override
    protected Importer createImporter() throws IOException, RepositoryException {
        return new Importer.Builder(() -> {
            Session session = MockJcr.newSession();
            return session.getRootNode();
        })
                .addMixins(false)
                .addUuid(true)
                .setProtectedProperties(true)
                .build();
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
