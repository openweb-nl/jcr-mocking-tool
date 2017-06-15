package nl.openweb.jcr;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;


import static org.junit.Assert.assertEquals;

/**
 * @author Ebrahim Aharpour
 * @since 6/14/2017
 */
public class UnknowPropertyTest {

    @Test
    public void test() throws RepositoryException, IOException, URISyntaxException {

        try (InMemoryJcrRepository inMemoryJcrRepository = new InMemoryJcrRepository()) {
            Node rootNode = new Importer.Builder(() -> {
                Session session = inMemoryJcrRepository.login(new SimpleCredentials("admin", "admin".toCharArray()));
                return session.getRootNode();
            }).addUnknownTypes(true).build().createNodesFromJson("{\"namespace:unknown\": \"value01\", \"namespaceLessProperty\": \"value02\"}");
            assertEquals("value01", rootNode.getProperty("namespace:unknown").getString());
            assertEquals("value02", rootNode.getProperty("namespaceLessProperty").getString());

        }
    }
}
