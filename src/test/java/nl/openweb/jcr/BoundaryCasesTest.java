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

import javax.jcr.*;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;


import static org.junit.Assert.assertEquals;

/**
 * @author Ebrahim Aharpour
 * @since 6/14/2017
 */
public class BoundaryCasesTest {

    @Test
    public void unknownPropertyTest() throws RepositoryException, IOException, URISyntaxException {

        try (InMemoryJcrRepository inMemoryJcrRepository = new InMemoryJcrRepository()) {
            Node rootNode = new Importer.Builder(() -> {
                Session session = inMemoryJcrRepository.login(new SimpleCredentials("admin", "admin".toCharArray()));
                return session.getRootNode();
            }).addUnknownTypes(true).build().createNodesFromJson("{\"namespace:unknown\": \"value01\", \"namespaceLessProperty\": \"value02\"}");
            assertEquals("value01", rootNode.getProperty("namespace:unknown").getString());
            assertEquals("value02", rootNode.getProperty("namespaceLessProperty").getString());

        }
    }

    @Test
    public void nullValuePropertyTest() throws IOException, RepositoryException, URISyntaxException {
        try (InMemoryJcrRepository inMemoryJcrRepository = new InMemoryJcrRepository()) {
            Node rootNode = new Importer.Builder(() -> {
                Session session = inMemoryJcrRepository.login(new SimpleCredentials("admin", "admin".toCharArray()));
                return session.getRootNode();
            }).addUnknownTypes(true).build().createNodesFromJson("{\"namespace:unknown\": null}");
            Assert.assertFalse(rootNode.hasNode("namespace:unknown"));
            Assert.assertFalse(rootNode.hasProperty("namespace:unknown"));
        }
    }

    @Test
    public void nullValuePropertyTest2() throws IOException, RepositoryException, URISyntaxException {
        try (InMemoryJcrRepository inMemoryJcrRepository = new InMemoryJcrRepository()) {
            Node rootNode = new Importer.Builder(() -> {
                Session session = inMemoryJcrRepository.login(new SimpleCredentials("admin", "admin".toCharArray()));
                return session.getRootNode();
            }).addUnknownTypes(true).build().createNodesFromJson("{\"namespace:unknown\": { \"primitiveType\" : \"Name\", \"value\" : null  }}");
            Assert.assertFalse(rootNode.hasNode("namespace:unknown"));
            Assert.assertFalse(rootNode.hasProperty("namespace:unknown"));
        }
    }

    @Test
    public void multiValuePropertyWithNullValue() throws IOException, RepositoryException, URISyntaxException {
        try (InMemoryJcrRepository inMemoryJcrRepository = new InMemoryJcrRepository()) {
            Node rootNode = new Importer.Builder(() -> {
                Session session = inMemoryJcrRepository.login(new SimpleCredentials("admin", "admin".toCharArray()));
                return session.getRootNode();
            }).addUnknownTypes(true).build().createNodesFromJson("{\"namespace:unknown\": [ null , \"value01\", \"value02\"]}");
            Property property = rootNode.getProperty("namespace:unknown");
            Assert.assertTrue(property.isMultiple());
            Value[] values = property.getValues();
            String[] expectedValues = {"value01", "value02"};
            for (int i = 0; i < values.length; i++) {
                assertEquals(expectedValues[i], values[i].getString());
            }
        }
    }
}
