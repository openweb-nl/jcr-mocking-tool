package nl.openweb.jcr;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.junit.Test;


import static org.junit.Assert.assertEquals;

/**
 * @author Ebrahim Aharpour
 * @since 9/3/2017
 */
public class ImportPathXmlTest extends AbstractImporterXmlTest {

    private InMemoryJcrRepository inMemoryJcrRepository;

    @Override
    protected String getImportPath() {
        return "some/path/";
    }

    @Override
    public void init() throws Exception {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("nodes.xml")) {
            Importer importer = createImporter();
            rootNode = importer.createNodesFromXml(inputStream, getImportPath());
        }
    }

    @Override
    protected Importer createImporter() throws IOException, RepositoryException, URISyntaxException {
        inMemoryJcrRepository = new InMemoryJcrRepository();
        return new Importer.Builder(() -> {
            Session session = inMemoryJcrRepository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            return session.getRootNode();
        })
                .addMixins(true)
                .addUuid(true)
                .addUnknownTypes(true)
                .saveSession(true)
                .build();
    }

    @Override
    protected void shutdown() throws IOException {
        inMemoryJcrRepository.shutdown();
    }

    @Test
    public void intermediateNodeTest() throws RepositoryException {
        Node someNode = rootNode.getNode("some");
        assertEquals("nt:unstructured", someNode.getPrimaryNodeType().getName());
        assertEquals("jmt:folder", someNode.getNode("path").getPrimaryNodeType().getName());

    }
}
