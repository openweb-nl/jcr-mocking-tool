package nl.openweb.jcr;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.io.IOException;

import org.junit.Test;

public class ErrorHandlingTest {

    @Test(expected = JcrImporterException.class)
    public void supplierThrowsException() throws IOException, RepositoryException {
        Importer importer = new Importer.Builder(() -> {
            throw new RepositoryException();
        }).build();
        importer.createNodesFromJson("{}");
    }

    @Test
    public void addUuidWhileNotSupported() throws Exception {
        try (InMemoryJcrRepository inMemoryJcrRepository = new InMemoryJcrRepository()){
            Importer importer = new Importer.Builder(() -> {
                Session session = inMemoryJcrRepository.login(new SimpleCredentials("admin", "admin".toCharArray()));
                return session.getRootNode();
            }).addUuid(true).build();
            importer.createNodesFromJson("{}");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullSupplier() throws IOException, RepositoryException {
        Importer importer = new Importer.Builder(null).addUuid(true).build();
        importer.createNodesFromJson("{}");
    }

    @Test(expected = JcrImporterException.class)
    public void malformedXmlTest() throws Exception {
        try (InMemoryJcrRepository inMemoryJcrRepository = new InMemoryJcrRepository()){
            Importer importer = new Importer.Builder(() -> {
                Session session = inMemoryJcrRepository.login(new SimpleCredentials("admin", "admin".toCharArray()));
                return session.getRootNode();
            }).addUuid(true).build();
            importer.createNodesFromXml("{}");
        }
    }

    @Test(expected = JcrImporterException.class)
    public void unexpectedXmlTest() throws Exception {
        try (InMemoryJcrRepository inMemoryJcrRepository = new InMemoryJcrRepository()){
            Importer importer = new Importer.Builder(() -> {
                Session session = inMemoryJcrRepository.login(new SimpleCredentials("admin", "admin".toCharArray()));
                return session.getRootNode();
            }).addUuid(true).build();
            importer.createNodesFromXml(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                            "<sv:property xmlns:sv=\"http://www.jcp.org/jcr/sv/1.0\" sv:name=\"rootNodeProperty\" sv:type=\"String\">" +
                            "<sv:value>rootNodePropertyValue</sv:value>" +
                            "</sv:property>");
        }
    }
}
