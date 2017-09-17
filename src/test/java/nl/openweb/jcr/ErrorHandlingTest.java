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
import java.io.InputStream;

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
    public void nullXmlInputSteamTest() throws Exception {
        try (InMemoryJcrRepository inMemoryJcrRepository = new InMemoryJcrRepository()){
            Importer importer = new Importer.Builder(() -> {
                Session session = inMemoryJcrRepository.login(new SimpleCredentials("admin", "admin".toCharArray()));
                return session.getRootNode();
            }).addUuid(false).build();
            importer.createNodesFromXml((InputStream) null);
        }
    }


    @Test(expected = JcrImporterException.class)
    public void nullJsonInputSteamTest() throws Exception {
        try (InMemoryJcrRepository inMemoryJcrRepository = new InMemoryJcrRepository()){
            Importer importer = new Importer.Builder(() -> {
                Session session = inMemoryJcrRepository.login(new SimpleCredentials("admin", "admin".toCharArray()));
                return session.getRootNode();
            }).addUuid(true).build();
            importer.createNodesFromJson((InputStream) null);
        }
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
