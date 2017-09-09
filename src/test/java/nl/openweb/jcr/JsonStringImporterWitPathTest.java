package nl.openweb.jcr;

/**
 * @author Ebrahim Aharpour
 * @since 9/3/2017
 */
public class JsonStringImporterWitPathTest extends AbstractImporterWithPathTest {

    @Override
    public void init() throws Exception {
        Importer importer = createImporter();
        rootNode = importer.createNodesFromJson(loadFileAsString("nodes.json"), getImportPath());
    }
}
