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
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

/**
 * @author Ebrahim Aharpour
 * @since 9/9/2017
 */
public class ImporterReflectionTest {

    @Test
    public void importerTest() throws RepositoryException, IOException, URISyntaxException {

        try (InMemoryJcrRepository inMemoryJcrRepository = new InMemoryJcrRepository()) {
            Session session = inMemoryJcrRepository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            JcrImporter importer = new JsonImporter(session.getRootNode())
                    .addMixins(true)
                    .addUuid(true)
                    .addUnknownTypes(true)
                    .saveSession(true);
            Node rootNode = importer.createNodes("{\n" +
                    "  \"subnode\": {\n" +
                    "    \"jcr:uuid\": \"e01ee3c8-dcbf-4bf8-9dc7-e08a425c259e\",\n" +
                    "    \"ns:stringProperty\": \"value\",\n" +
                    "    \"ns:longProperty\": 4,\n" +
                    "    \"subsubnode\": {\n" +
                    "      \"jcr:primaryType\": \"ns:subtype\",\n" +
                    "      \"jcr:uuid\": \"16e2251d-1b33-438a-801f-9ce0ee6accaa\"\n" +
                    "    },\n" +
                    "    \"subsubnode2\": {\n" +
                    "      \"jcr:primaryType\": \"\",\n" +
                    "      \"jcr:uuid\": \"689a5373-4a94-4818-aaef-273e8d2d8836\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "}");
            Node subnode = rootNode.getNode("subnode");
            assertEquals("e01ee3c8-dcbf-4bf8-9dc7-e08a425c259e", subnode.getIdentifier());
            assertEquals("nt:unstructured", subnode.getPrimaryNodeType().getName());
            assertEquals("value", subnode.getProperty("ns:stringProperty").getString());
            assertEquals(4L, subnode.getProperty("ns:longProperty").getLong());
            Node subsubnode = rootNode.getNode("subnode/subsubnode");
            assertEquals("16e2251d-1b33-438a-801f-9ce0ee6accaa", subsubnode.getIdentifier());
            assertEquals("ns:subtype", subsubnode.getPrimaryNodeType().getName());
            Node subsubnod2 = rootNode.getNode("subnode/subsubnode2");
            assertEquals("689a5373-4a94-4818-aaef-273e8d2d8836", subsubnod2.getIdentifier());
            assertEquals("nt:unstructured", subsubnod2.getPrimaryNodeType().getName());
        }


    }


}
