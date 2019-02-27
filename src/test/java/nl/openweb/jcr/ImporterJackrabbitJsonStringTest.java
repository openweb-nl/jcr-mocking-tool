/*
 * Copyright 2017 Open Web IT B.V. (https://www.openweb.nl/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.openweb.jcr;

import nl.openweb.jcr.importer.JcrImporter;
import nl.openweb.jcr.importer.JsonImporter;
import nl.openweb.jcr.importer.XmlImporter;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.io.IOException;
import java.net.URISyntaxException;

public class ImporterJackrabbitJsonStringTest extends AbstractImporterTest {
    private InMemoryJcrRepository inMemoryJcrRepository;

    @Override
    public void init() throws Exception {
        String json = loadFileAsString("nodes.json");
        JcrImporter importer = createImporter(JsonImporter.FORMAT);
        rootNode = importer.createNodes(json);
    }

    @Override
    protected JcrImporter createImporter(String format) throws IOException, RepositoryException, URISyntaxException {
        inMemoryJcrRepository = new InMemoryJcrRepository();
        Session session = inMemoryJcrRepository.login(new SimpleCredentials("admin", "admin".toCharArray()));
        JcrImporter importer;
        if (JsonImporter.FORMAT.equals(format)) {
            importer = new JsonImporter(session.getRootNode());
        } else if (XmlImporter.FORMAT.equals(format)) {
            importer = new XmlImporter(session.getRootNode());
        } else {
            throw new IllegalArgumentException("Unknown format: " + format);
        }

        return importer
                .addMixins(true)
                .addUuid(true)
                .addUnknownTypes(true)
                .saveSession(true);
    }

    @Override
    protected void shutdown() throws IOException {
        inMemoryJcrRepository.shutdown();
    }

}
