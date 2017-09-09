package nl.openweb.jcr;

import java.io.InputStream;

/**
 * @author Ebrahim Aharpour
 * @since 9/3/2017
 */
public class JsonImporterWitPathTest extends AbstractImporterWithPathTest {

    @Override
    public void init() throws Exception {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("nodes.json")) {
            Importer importer = createImporter();
            importer.createNodesFromJson("{}", "/some");
            rootNode = importer.createNodesFromJson(inputStream, getImportPath());
        }
    }
}
