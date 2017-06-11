package nl.openweb.jcr;

import java.io.InputStream;

public abstract class AbstractImporterXmlTest extends AbstractImporterTest {

    @Override
    public void init() throws Exception {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("nodes.xml")) {
            Importer importer = createImporter();
            rootNode = importer.createNodesFromXml(inputStream);
        }
    }
}
