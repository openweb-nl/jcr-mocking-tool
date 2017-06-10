package nl.openweb.jcr;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.io.IOException;
import java.net.URISyntaxException;

public class ImporterJackrabbitTest extends AbstractImporterTest {
    private InMemoryJcrRepository inMemoryJcrRepository;

    @Override
    protected Importer createImporter() throws IOException, RepositoryException, URISyntaxException {
        inMemoryJcrRepository = new InMemoryJcrRepository();
        return new Importer.Builder(() -> {
            Session session = inMemoryJcrRepository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            return session.getRootNode();
        })
                .addMixins(true)
                .addUuid(false)
                .addUnknownTypes(true)
                .build();
    }

    @Override
    protected void shutdown() throws IOException {
        inMemoryJcrRepository.shutdown();
    }
}
