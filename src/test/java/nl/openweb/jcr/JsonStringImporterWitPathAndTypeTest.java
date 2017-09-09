package nl.openweb.jcr;

/**
 * @author Ebrahim Aharpour
 * @since 9/3/2017
 */
public class JsonStringImporterWitPathAndTypeTest extends AbstractImporterWithPathTest {

    private static final String INTERMEDIATE_TYPE = "fake:intermediateType";

    @Override
    protected String expectedIntermediateNodeType() {
        return INTERMEDIATE_TYPE;
    }

    @Override
    public void init() throws Exception {
        Importer importer = createImporter();
        rootNode = importer.createNodesFromJson(loadFileAsString("nodes.json"), getImportPath(), INTERMEDIATE_TYPE);
    }
}
