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

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Ebrahim Aharpour
 * @since 12/7/2017
 */
public class importerSupplierTest {
    private int counter = 0;

    @Test
    public void makingSureThatSupplierIsCallOnlyOnce() throws Exception {
        try (InMemoryJcrRepository inMemoryJcrRepository = new InMemoryJcrRepository()) {

            Importer importer = new Importer.Builder(() -> {
                Session session = inMemoryJcrRepository.login(new SimpleCredentials("admin", "admin".toCharArray()));
                counter++;
                return session.getRootNode();
            })
                    .addMixins(true)
                    .addUuid(true)
                    .addUnknownTypes(true)
                    .saveSession(true)
                    .build();

            importer.createNodesFromJson("{\"node\": {\"property\":\"value\"}}");
            Node rootNode = importer.createNodesFromJson("{\"node2\": {\"property\":\"value\"}}", "/some/path");
            assertEquals(1, counter);

            assertEquals("/", rootNode.getPath());
            assertTrue( rootNode.hasNode("node"));
            assertTrue("/", rootNode.hasNode("some/path/node2"));

        }

    }

}
