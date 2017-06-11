package nl.openweb.jcr;

import java.io.InputStream;

public abstract class AbstractImporterJsonTest extends AbstractImporterTest {

    @Override
    public void init() throws Exception {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("nodes.json")) {
            Importer importer = createImporter();
            rootNode = importer.createNodesFromJson(inputStream);
        }
    }
}
