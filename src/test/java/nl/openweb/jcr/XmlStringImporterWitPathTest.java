package nl.openweb.jcr;

/**
 * @author Ebrahim Aharpour
 * @since 9/3/2017
 */
public class XmlStringImporterWitPathTest extends AbstractImporterWithPathTest {

    @Override
    public void init() throws Exception {
        Importer importer = createImporter();
        rootNode = importer.createNodesFromXml(loadFileAsString("nodes.xml"), getImportPath());
    }
}
