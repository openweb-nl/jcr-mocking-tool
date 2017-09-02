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

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class ImporterJackrabbitJsonStringTest extends AbstractImporterTest {
    private InMemoryJcrRepository inMemoryJcrRepository;

    @Override
    public void init() throws Exception {
        String json = loadJson("nodes.json");
        Importer importer = createImporter();
        rootNode = importer.createNodesFromJson(json);
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

    private String loadJson(String file) throws URISyntaxException, IOException {
        URI uri = getClass().getClassLoader().getResource(file).toURI();
        return Files.readAllLines(Paths.get(uri)).stream().collect(Collectors.joining("\n"));
    }
}
