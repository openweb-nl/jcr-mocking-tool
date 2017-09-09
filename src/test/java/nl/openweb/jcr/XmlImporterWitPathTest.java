package nl.openweb.jcr;

import java.io.InputStream;

/**
 * @author Ebrahim Aharpour
 * @since 9/3/2017
 */
public class XmlImporterWitPathTest extends AbstractImporterWithPathTest {

    @Override
    public void init() throws Exception {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("nodes.xml")) {
            Importer importer = createImporter();
            rootNode = importer.createNodesFromXml(inputStream, getImportPath());
        }
    }
}
