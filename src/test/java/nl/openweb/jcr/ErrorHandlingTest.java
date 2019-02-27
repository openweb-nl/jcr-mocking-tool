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
import org.junit.Test;

import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.io.InputStream;

public class ErrorHandlingTest {

    @Test
    public void addUuidWhileNotSupported() throws Exception {
        try (InMemoryJcrRepository inMemoryJcrRepository = new InMemoryJcrRepository()){
            Session session = inMemoryJcrRepository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            JcrImporter importer = new JsonImporter(session.getRootNode());
            importer.createNodes("{}");
        }
    }

    @Test(expected = JcrImporterException.class)
    public void nullXmlInputSteamTest() throws Exception {
        try (InMemoryJcrRepository inMemoryJcrRepository = new InMemoryJcrRepository()){
            Session session = inMemoryJcrRepository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            JcrImporter importer = new XmlImporter(session.getRootNode()).addUuid(false);
            importer.createNodes((InputStream) null);
        }
    }


    @Test(expected = JcrImporterException.class)
    public void nullJsonInputSteamTest() throws Exception {
        try (InMemoryJcrRepository inMemoryJcrRepository = new InMemoryJcrRepository()){
            Session session = inMemoryJcrRepository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            JcrImporter importer = new JsonImporter(session.getRootNode()).addUuid(true);
            importer.createNodes((InputStream) null);
        }
    }

    @Test(expected = JcrImporterException.class)
    public void malformedXmlTest() throws Exception {
        try (InMemoryJcrRepository inMemoryJcrRepository = new InMemoryJcrRepository()){
            Session session = inMemoryJcrRepository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            JcrImporter importer = new XmlImporter(session.getRootNode()).addUuid(true);
            importer.createNodes("{}");
        }
    }

    @Test(expected = JcrImporterException.class)
    public void unexpectedXmlTest() throws Exception {
        try (InMemoryJcrRepository inMemoryJcrRepository = new InMemoryJcrRepository()){
            Session session = inMemoryJcrRepository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            JcrImporter importer = new XmlImporter(session.getRootNode()).addUuid(true);
            importer.createNodes(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                            "<sv:property xmlns:sv=\"http://www.jcp.org/jcr/sv/1.0\" sv:name=\"rootNodeProperty\" sv:type=\"String\">" +
                            "<sv:value>rootNodePropertyValue</sv:value>" +
                            "</sv:property>");
        }
    }
}
